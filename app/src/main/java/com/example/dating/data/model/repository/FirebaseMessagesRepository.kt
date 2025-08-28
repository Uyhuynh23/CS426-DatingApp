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
        val currentUid = auth.currentUser?.uid ?: return@callbackFlow
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
}
