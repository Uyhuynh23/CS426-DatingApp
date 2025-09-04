package com.example.dating.data.model.repository

import android.content.Context
import com.example.dating.data.model.User
import com.example.dating.data.model.utils.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.sqrt

class RecommendationRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    // --- Config: predefined interests ---
    private val allInterests = listOf(
        "music", "travel", "cooking", "sports", "reading",
        "movies", "art", "gaming", "fitness", "photography",
        "dancing", "fashion", "technology", "pets"
    )

    // --- Load job embeddings from JSON asset ---
    private val jobEmbeddingMap: Map<String, FloatArray> = loadJobEmbeddings(context, "job_embeddings_8d.json")

    private fun loadJobEmbeddings(context: Context, assetFile: String): Map<String, FloatArray> {
        val jsonStr = context.assets.open(assetFile).bufferedReader().use { it.readText() }
        val json = JSONObject(jsonStr)
        val map = mutableMapOf<String, FloatArray>()
        json.keys().forEach { key ->
            val arr = json.getJSONArray(key)
            val floatArr = FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
            map[key] = floatArr
        }
        return map
    }

    // --- Placeholder for text embedding model (e.g., SentenceTransformer / TFLite) ---
    private fun textEmbedding(text: String, dim: Int = 384): FloatArray {
        val hash = text.hashCode()
        return FloatArray(dim) { i -> ((hash + i) % 100) / 100f }
    }

    suspend fun getEmbeddingFromFirestore(userId: String): FloatArray? {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val embeddingDoc = db.collection("user_embeddings").document(userId).get().await()
        if (embeddingDoc.exists()) {
            val embeddingMap = embeddingDoc.data ?: emptyMap<String, Any>()
            return FloatArray(embeddingMap.size) { i ->
                (embeddingMap["dim_$i"] as? Number)?.toFloat() ?: 0f
            }
        }
        return null
    }

    private fun saveEmbeddingToFirestore(userId: String, userEmbedding: FloatArray) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val embeddingMap = userEmbedding.mapIndexed { i, v -> "dim_$i" to v }.toMap()
        db.collection("user_embeddings").document(userId)
            .set(embeddingMap)
            .addOnSuccessListener {
                android.util.Log.d("RecommendationRepository", "Saved embedding for $userId")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("RecommendationRepository", "Failed to save embedding for $userId", e)
            }
    }

    // --- Create user embedding ---
    suspend fun createEmbedding(user: User): FloatArray {
        getEmbeddingFromFirestore(user.uid)?.let { return it }

        // --- Age ---
        val age = DateUtils.calculateAgeFromBirthday(user.birthday) ?: 0
        val ageFeature = ((age - 18) / 42f).coerceIn(0f, 1f)


        // --- Last Active ---
        val now = System.currentTimeMillis()
        val daysSinceLastActive = if (user.lastActive != null && user.lastActive > 0) {
            ((now - user.lastActive) / (1000 * 60 * 60 * 24)).toFloat()
        } else 365f
        val lastActiveFeature = (daysSinceLastActive / 365f).coerceIn(0f, 1f)

        // --- Interests ---
        val interestFeature = FloatArray(allInterests.size) { 0f }
        user.interests.forEach { interest ->
            val index = allInterests.indexOf(interest)
            if (index != -1) interestFeature[index] = 1f
        }

        // --- Job embedding 8-dim ---
        val jobFeature = user.job?.let { jobEmbeddingMap[it] } ?: FloatArray(8) { 0f }

        // --- Description embedding ---
        val descriptionFeature = if (!user.description.isNullOrBlank()) {
            textEmbedding(user.description)
        } else {
            FloatArray(384) { 0f }
        }

        // --- Concat all features ---
        val embedding = floatArrayOf(ageFeature, lastActiveFeature) +
                interestFeature +
                jobFeature +
                descriptionFeature
        saveEmbeddingToFirestore(user.uid, embedding)
        return embedding
    }

    // --- Similarity ---
    fun dotProduct(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        for(i in a.indices) sum += a[i] * b[i]
        return sum
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val dot = dotProduct(a, b)
        val normA = sqrt(a.map { it * it }.sum())
        val normB = sqrt(b.map { it * it }.sum())
        return if (normA > 0 && normB > 0) dot / (normA * normB) else 0f
    }

    // --- Compute scores ---
    suspend fun computeScores(
        currentEmbedding: FloatArray,
        users: List<User>,
        useCosine: Boolean = false
    ): List<Pair<User, Float>> = coroutineScope {
        users.map { user ->
            async {
                val embedding = createEmbedding(user)
                val score = if (useCosine) cosineSimilarity(currentEmbedding, embedding) else dotProduct(currentEmbedding, embedding)
                user to score
            }
        }.map { it.await() }
    }

    fun sortUsersByScore(scoredUsers: List<Pair<User, Float>>): List<User> {
        return scoredUsers.sortedByDescending { it.second }.map { it.first }
    }

    // --- Main recommendation ---
    suspend fun getRecommendedUsers(currentUser: User, users: List<User>): List<User> {
        val currentEmbedding = createEmbedding(currentUser)
        val scoredUsers = computeScores(currentEmbedding, users)

        // chỉ lấy score dương
        val filtered = scoredUsers.filter { it.second > 0f }
            .sortedByDescending { it.second }

        val batchSize = (filtered.size / 3).coerceAtLeast(1) // tránh chia 0
        val batches = filtered.chunked(batchSize)

        val shuffledBatches = batches.mapIndexed { index, batch ->
            val shuffled = batch.shuffled()
            android.util.Log.d("RecommendationRepository", "Batch $index size: ${shuffled.size}")
            shuffled.forEach { (user, score) ->
                android.util.Log.d("RecommendationRepository", "Batch $index -> User: ${user.uid}, ${user.firstName} ${user.lastName}, Score: $score")
            }
            shuffled
        }

        // gộp lại thành danh sách cuối cùng
        return shuffledBatches.flatten().map { it.first }
    }



    // --- Update embedding online khi swipe ---
    fun updateEmbeddingWithFeedback(currentUserId: String, userEmbedding: FloatArray, otherEmbedding: FloatArray, liked: Boolean, learningRate: Float = 0.05f) {
        val sign = if (liked) 1f else -1f
        for(i in userEmbedding.indices) {
            userEmbedding[i] += learningRate * sign * otherEmbedding[i]
        }
        saveEmbeddingToFirestore(currentUserId, userEmbedding)
    }
}
