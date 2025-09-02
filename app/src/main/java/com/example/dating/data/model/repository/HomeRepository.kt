package com.example.dating.data.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.dating.data.model.User
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun fetchProfiles(): List<String> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("HomeRepository", "Fetched UIDs: $currentUserId")

        val snapshot = db.collection("users").get().await()
        val uids = snapshot.documents.mapNotNull { doc ->
            val uid = doc.id
            if (uid != currentUserId) uid else null
        }
        android.util.Log.d("HomeRepository", "Fetched UIDs: $uids")
        return uids
    }

    suspend fun getUserProfilesByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        val batchSize = 30
        val batches = userIds.chunked(batchSize)
        val allUsers = mutableListOf<User>()
        for (batch in batches) {
            val snapshot = db.collection("users")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
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
            allUsers.addAll(users)
        }
        return allUsers
    }
}
