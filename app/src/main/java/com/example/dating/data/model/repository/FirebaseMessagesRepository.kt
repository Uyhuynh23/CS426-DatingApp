package com.example.dating.data.model.repository

import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.data.model.MessagePreview
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
                        val lastMessage = (data["lastMessage"] as? Map<*, *>)?.let { msgMap ->
                            MessagePreview(
                                fromUid = msgMap["fromUid"] as? String ?: "",
                                text = msgMap["text"] as? String ?: "",
                                timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                            )
                        }
                        val timestamp = when(val t = data["lastTimestamp"]) {
                            is Number -> t.toLong()
                            is Timestamp -> t.toDate().time
                            else -> 0L
                        }
                        val unread = (data["unread"] as? Map<*, *>)?.get(currentUid) as? Long ?: 0L
                        val typing = (data["typing"] as? Map<*, *>)?.get(peerUid) as? Boolean ?: false

                        ConversationPreview(
                            currentUid = currentUid,
                            id = cid,
                            peer = user,
                            lastMessage = lastMessage,
                            lastMessageTimestamp = timestamp,
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
        val now = System.currentTimeMillis()
        android.util.Log.d("formatTimeAgo", "now: $now, ms: $ms")
        val diff = now - ms

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""}"
            days < 7 -> "$days day${if (days > 1) "s" else ""}"
            weeks < 4 -> "$weeks week${if (weeks > 1) "s" else ""}"
            months < 12 -> "$months month${if (months > 1) "s" else ""}"
            else -> "$years year${if (years > 1) "s" else ""}"
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