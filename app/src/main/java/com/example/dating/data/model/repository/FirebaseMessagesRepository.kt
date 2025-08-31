package com.example.dating.data.model.repository

import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessagesRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getConversations(): Flow<List<ConversationPreview>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if(currentUid == null) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val query = db.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val tasks = snapshot.documents.map { doc ->
                val data = doc.data ?: return@map null
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
        // First check if a conversation already exists between these users
        val existingConversation = db.collection("conversations")
            .whereArrayContains("participants", userId1)
            .get()
            .await()
            .documents
            .find { doc ->
                val participants = doc.get("participants") as? List<String>
                participants?.contains(userId2) == true
            }

        if (existingConversation != null) {
            // Conversation already exists, no need to create a new one
            return
        }

        // Get user names for the match message
        val user1Doc = db.collection("users").document(userId1).get().await()
        val user2Doc = db.collection("users").document(userId2).get().await()

        val user1Name = user1Doc.getString("firstName") ?: "Someone"
        val user2Name = user2Doc.getString("firstName") ?: "Someone"

        // Create conversation
        val timestamp = System.currentTimeMillis()

        // Create the conversation document with initial data
        val conversationData = hashMapOf(
            "participants" to listOf(userId1, userId2),
            "lastMessage" to "You matched with $user2Name!",
            "lastTimestamp" to timestamp,
            "unread" to hashMapOf(
                userId1 to 0,
                userId2 to 1
            ),
            "typing" to hashMapOf(
                userId1 to false,
                userId2 to false
            )
        )

        val conversationRef = db.collection("conversations").document()
        val conversationId = conversationRef.id

        // Save the conversation
        conversationRef.set(conversationData).await()

        // Add the first system message about the match
        val matchMessage = hashMapOf(
            "text" to "You matched with $user2Name!",
            "senderId" to "system",
            "timestamp" to timestamp,
            "read" to false
        )

        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(matchMessage)
            .await()
    }
}
