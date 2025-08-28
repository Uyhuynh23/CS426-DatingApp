package com.example.dating.viewmodel
import androidx.lifecycle.ViewModel
import com.example.dating.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun loadConversation(conversationId: String) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject(ChatMessage::class.java)?.copy(id = d.id)
                }.orEmpty()
                _messages.value = list
            }
    }

    fun sendMessage(conversationId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val msg = mapOf(
            "fromUid" to uid,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(msg)
    }
}