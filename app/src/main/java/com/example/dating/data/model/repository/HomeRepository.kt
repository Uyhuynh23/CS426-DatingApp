package com.example.dating.data.model.repository

import com.example.dating.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val filteringRepository: FilteringRepository, // Remove RecommendationRepository injection
    private val recommendationRepository: RecommendationRepository // Remove RecommendationRepository injection
) {
    suspend fun fetchProfiles(): List<String> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) return emptyList()
        val currentUserDoc = db.collection("users").document(currentUserId).get().await()
        val prefsMap = currentUserDoc.get("filterPreferences") as? Map<*, *>
        val filterPrefs = prefsMap?.let {
            com.example.dating.data.model.UserFilterPreferences(
                preferredGender = it["preferredGender"] as? String,
                minAge = (it["minAge"] as? Long)?.toInt(),
                maxAge = (it["maxAge"] as? Long)?.toInt(),
                maxDistance = (it["maxDistance"] as? Long)?.toInt()
            )
        }
        val snapshot = db.collection("users").get().await()
        val filteredDocs = snapshot.documents.filter { doc ->
            filteringRepository.filterUser(doc, currentUserId, currentUserDoc, filterPrefs)
        }
        // Get current user object for recommendation
        val currentUser = getUserProfilesByIds(listOf(currentUserId)).firstOrNull()
        val users = getUserProfilesByIds(filteredDocs.map { it.id })
        android.util.Log.d("HomeRepository", "Before recommendation: users=${users.size}, currentUser=$currentUser")
        val sortedDocs = if (currentUser != null) {
            val recommended = recommendationRepository.getRecommendedUsers(currentUser, users)
            android.util.Log.d("HomeRepository", "After recommendation: recommended=${recommended.size}")
            recommended
        } else {
            users
        }
        val uids = sortedDocs.map { it.uid }
        android.util.Log.d("HomeRepository", "Filtered & Sorted UIDs: $uids")
        return uids
    }

    suspend fun getUserProfilesByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        val batchSize = 30
        val batches = userIds.chunked(batchSize)
        val userMap = mutableMapOf<String, User>()

        for (batch in batches) {
            val snapshot = db.collection("users")
                .whereIn(FieldPath.documentId(), batch)
                .get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                User(
                    uid = doc.id,
                    firstName = data["firstName"] as? String ?: "",
                    lastName = data["lastName"] as? String ?: "",
                    birthday = data["birthday"] as? String,
                    imageUrl = (data["imageUrl"] as? List<String>) ?: emptyList(),
                    avatarUrl = data["avatarUrl"] as? String,
                    gender = data["gender"] as? String,
                    job = data["job"] as? String,
                    location = data["location"] as? String,
                    description = data["description"] as? String,
                    interests = (data["interests"] as? List<String>) ?: emptyList(),
                    distance = (data["distance"] as? Long)?.toInt()
                )
            }
            // Lưu vào map theo uid
            users.forEach { userMap[it.uid] = it }
        }

        // Reorder theo thứ tự của userIds đầu vào
        return userIds.mapNotNull { userMap[it] }
    }


    suspend fun saveUserLocation(uid: String, location: Map<String, Any>) {
        db.collection("users").document(uid)
            .set(location, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }



}
