package com.example.dating.data.model.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.dating.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addFavorite(likerId: String, likedId: String): Boolean {
        val favorite = hashMapOf(
            "likerId" to likerId,
            "likedId" to likedId,
            "timestamp" to Timestamp.now()
        )
        val query = db.collection("favorites")
            .whereEqualTo("likerId", likerId)
            .whereEqualTo("likedId", likedId)
            .get().await()
        return if (query.isEmpty) {
            db.collection("favorites").add(favorite)
            true
        } else {
            false
        }
    }

    suspend fun isMatch(likerId: String, likedId: String): Boolean {
        val matchQuery = db.collection("favorites")
            .whereEqualTo("likerId", likedId)
            .whereEqualTo("likedId", likerId)
            .get().await()
        return !matchQuery.isEmpty
    }

    suspend fun deleteFavorite(likerId: String, likedId: String) {
        val query = db.collection("favorites")
            .whereEqualTo("likerId", likerId)
            .whereEqualTo("likedId", likedId)
            .get().await()
        for (doc in query.documents) {
            db.collection("favorites").document(doc.id).delete().await()
        }
    }

    suspend fun getFavoritesByLikedId(likedId: String): List<Map<String, Any>> {
        val query = db.collection("favorites")
            .whereEqualTo("likedId", likedId)
            .get().await()
        return query.documents.mapNotNull { it.data }
    }

    suspend fun getUserProfilesByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        val snapshot = db.collection("users")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds.take(9))
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
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
    }
}
