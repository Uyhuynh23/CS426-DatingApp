package com.example.dating.data.model.repository

import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessagesRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getConversations(currentUid: String): Flow<List<ConversationPreview>> = callbackFlow {
        if(currentUid.isBlank()) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val query = db.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)

        android.util.Log.d("FirebaseMessagesRepository", "Firestore query: participants contains $currentUid, ordered by lastTimestamp DESC")

        val listener = query.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            android.util.Log.d("FirebaseMessagesRepository", "Query result count: ${snapshot.documents.size}")
            snapshot.documents.forEach { doc ->
                android.util.Log.d(
                    "FirebaseMessagesRepository",
                    "Conversation doc: ${doc.id}, exists: ${doc.exists()}, fromCache: ${doc.metadata.isFromCache}, hasPendingWrites: ${doc.metadata.hasPendingWrites()}, data: ${doc.data}"
                )
            }

            val tasks = snapshot.documents.filter { doc ->
                doc.exists() && doc.data != null
            }
            .map { doc ->
                val data = doc.data!!
                val cid = doc.id
                val participants = data["participants"] as List<String>
                val peerUid = participants.first { it != currentUid }

                val userTask = db.collection("users").document(peerUid).get()

                userTask.continueWith { userSnap ->
                    val user = userSnap.result?.toObject(User::class.java) ?: User(uid = peerUid)
                    val lastMessage = data["lastMessage"] as? String ?: ""
                    val timestamp = (data["lastTimestamp"] as? Timestamp)?.toDate()?.time ?: 0L
                    val unread = (data["unread"] as? Map<*, *>)?.get(currentUid) as? Long ?: 0L
                    val typing = (data["typing"] as? Map<*, *>)?.get(peerUid) as? Boolean ?: false

                    ConversationPreview(
                        id = cid,
                        peer = user,
                        lastMessage = lastMessage,
                        timeAgo = formatTimeAgo(timestamp),
                        unreadCount = unread.toInt(),
                        isTyping = typing
                    )
                }
            }

            Tasks.whenAllSuccess<ConversationPreview>(tasks)
                .addOnSuccessListener { result -> trySend(result) }
        }

        awaitClose { listener.remove() }
    }

    private fun formatTimeAgo(ms: Long): String {
        val min = ((System.currentTimeMillis() - ms) / 60000)
        return when {
            min < 1 -> "Just now"
            min < 60 -> "$min min"
            else -> "${min / 60} hour"
        }
    }

    suspend fun createConversation(userId1: String, userId2: String) {
        // Check for existing conversation with both participants
        val existing = db.collection("conversations")
            .whereEqualTo("participants", listOf(userId1, userId2))
            .get().await()
        if (!existing.isEmpty) return // Already exists, do not add
        val existingReverse = db.collection("conversations")
            .whereEqualTo("participants", listOf(userId2, userId1))
            .get().await()
        if (!existingReverse.isEmpty) return // Already exists in reverse order

        val conversationData = hashMapOf(
            "participants" to listOf(userId1, userId2),
            "lastMessage" to "",
            "lastTimestamp" to System.currentTimeMillis(),
            "unread" to mapOf(userId1 to 0, userId2 to 0)
        )
        db.collection("conversations").add(conversationData).await()
    }
}
