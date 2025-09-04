package com.example.dating.data.model.repository

import android.content.Context
import com.example.dating.data.model.User
import com.example.dating.data.model.utils.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
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


    // --- Create user embedding ---
    fun createEmbedding(user: User): FloatArray {
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
        return floatArrayOf(ageFeature, lastActiveFeature) +
                interestFeature +
                jobFeature +
                descriptionFeature
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
    fun computeScores(
        currentEmbedding: FloatArray,
        users: List<User>,
        useCosine: Boolean = false
    ): List<Pair<User, Float>> {
        return users.map { user ->
            val embedding = createEmbedding(user)
            val score = if (useCosine) cosineSimilarity(currentEmbedding, embedding) else dotProduct(currentEmbedding, embedding)
            user to score
        }
    }

    fun sortUsersByScore(scoredUsers: List<Pair<User, Float>>): List<User> {
        return scoredUsers.sortedByDescending { it.second }.map { it.first }
    }

    // --- Main recommendation ---
    suspend fun getRecommendedUsers(currentUser: User, users: List<User>): List<User> {
        val currentEmbedding = createEmbedding(currentUser)
        val scoredUsers = computeScores(currentEmbedding, users)
        android.util.Log.d("RecommendationRepository", "scoredUsers size: ${scoredUsers.size}")
        scoredUsers.forEach { (user, score) ->
            android.util.Log.d("RecommendationRepository", "User: ${user.uid}, Name:${user.firstName} ${user.lastName}, Score: $score")
        }
        return sortUsersByScore(scoredUsers)
    }

    // --- Update embedding online khi swipe ---
    fun updateEmbeddingWithFeedback(userEmbedding: FloatArray, otherEmbedding: FloatArray, liked: Boolean, learningRate: Float = 0.01f) {
        val sign = if (liked) 1f else -1f
        for(i in userEmbedding.indices) {
            userEmbedding[i] += learningRate * sign * otherEmbedding[i]
        }
    }
}
