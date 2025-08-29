package com.example.dating.data.model.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteRepository {
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

    suspend fun getUserProfilesByIds(userIds: List<String>): List<Map<String, Any>> {
        if (userIds.isEmpty()) return emptyList()
        val snapshot = db.collection("users")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds.take(9))
            .get().await()
        return snapshot.documents.mapNotNull { it.data?.plus("uid" to it.id) }
    }
}
