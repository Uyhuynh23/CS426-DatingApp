package com.example.dating.data.model.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveMatch(userId1: String, userId2: String, status: Boolean) {
        // Ensure userId1 < userId2 alphabetically
        val (firstId, secondId) = if (userId1 < userId2) userId1 to userId2 else userId2 to userId1
        val matchData = hashMapOf(
            "userId1" to firstId,
            "userId2" to secondId,
            "timestamp" to Timestamp.now(),
            "status" to status
        )
        // Check for duplicate (userId1/userId2 only)
        val query = db.collection("match")
            .whereEqualTo("userId1", firstId)
            .whereEqualTo("userId2", secondId)
            .get().await()
        android.util.Log.d("MatchRepository", "MatchQuery: userId1=$firstId, userId2=$secondId, resultCount=${query.size()}")
        if (query.isEmpty) {
            db.collection("match").add(matchData)
        }
    }

    suspend fun getUserDocument(userId: String): Map<String, Any?> {
        val doc = db.collection("users").document(userId).get().await()
        return doc.data ?: emptyMap()
    }

}
