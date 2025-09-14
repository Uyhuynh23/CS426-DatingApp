package com.example.dating.data.model.repository

import com.example.dating.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.ASCENDING
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getPeerInfo(conversationId: String): Triple<String, String, Long?> {
        val doc = db.collection("conversations").document(conversationId).get().await()
        val currentUid = auth.currentUser?.uid
        val participants = doc.get("participants") as? List<String> ?: return Triple("", "", null)
        val peerId = participants.find { it != currentUid } ?: return Triple("", "", null)
        val userDoc = db.collection("users").document(peerId).get().await()
        val firstName = userDoc.getString("firstName") ?: ""
        val lastName = userDoc.getString("lastName") ?: ""
        val name = "$firstName $lastName".trim()
        val photoUrl = userDoc.getString("avatarUrl") ?: ""
        val lastActive = userDoc.getLong("lastActive") // <-- may be null
        return Triple(name, photoUrl, lastActive)
    }

    suspend fun getPeerInfoWithUid(conversationId: String): PeerInfoResult {
        val doc = db.collection("conversations").document(conversationId).get().await()
        val currentUid = auth.currentUser?.uid
        val participants = doc.get("participants") as? List<String> ?: return PeerInfoResult("", "", null, null, false)
        val peerId = participants.find { it != currentUid } ?: return PeerInfoResult("", "", null, null, false)
        val userDoc = db.collection("users").document(peerId).get().await()
        val firstName = userDoc.getString("firstName") ?: ""
        val lastName = userDoc.getString("lastName") ?: ""
        val name = "$firstName $lastName".trim()
        val photoUrl = userDoc.getString("avatarUrl") ?: ""
        val lastActive = userDoc.getLong("lastActive") // <-- may be null
        val isOnline = userDoc.getBoolean("isOnline") ?: false
        return PeerInfoResult(name, photoUrl, lastActive, peerId, isOnline)
    }

    data class PeerInfoResult(
        val name: String,
        val photoUrl: String,
        val lastActive: Long?,
        val peerUid: String?,
        val isOnline: Boolean // <-- Add isOnline here
    )

    fun getMessagesRealtime(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject(ChatMessage::class.java)?.copy(id = d.id)
                }.orEmpty()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }



    suspend fun sendMessage(conversationId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()
        val msg = mapOf(
            "fromUid" to uid,
            "text" to text,
            "timestamp" to timestamp
        )
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(msg)
            .await()
        // Update lastTimestamp and lastMessage in the conversation document
        // First, get all participants and active status
        val conversationDoc = db.collection("conversations").document(conversationId).get().await()
        val participants = conversationDoc.get("participants") as? List<String> ?: emptyList()
        val activeMap = conversationDoc.get("active") as? Map<String, Boolean> ?: emptyMap()
        val unreadUpdate = mutableMapOf<String, Any>()
        for (participant in participants) {
            unreadUpdate["unread.$participant"] = if (participant == uid || (activeMap[participant] == true)) 0 else FieldValue.increment(1)
        }
        db.collection("conversations")
            .document(conversationId)
            .update(
                mapOf(
                    "lastTimestamp" to timestamp,
                    "lastMessage" to msg,
                    "lastSender" to uid
                ) + unreadUpdate
            )
            .await()
    }

    // Count unread messages for a user in a conversation
    suspend fun getUnreadCount(conversationId: String, userId: String): Int {
        val doc = db.collection("conversations").document(conversationId).get().await()
        val unreadMap = doc.get("unread") as? Map<*, *>
        return (unreadMap?.get(userId) as? Number)?.toInt() ?: 0
    }

    // Update unread count for a user in a conversation
    suspend fun resetUnreadCount(conversationId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("conversations")
            .document(conversationId)
            .update("unread.$uid", 0)
            .await()
    }

    // Set active status for a user in a conversation
    suspend fun setActive(conversationId: String, isActive: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("conversations")
            .document(conversationId)
            .update("active.$uid", isActive)
            .await()
    }
}
