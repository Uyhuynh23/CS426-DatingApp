package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.MessagesFilterState
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.FirebaseMessagesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.dating.data.model.filterMessages

data class MessagesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ConversationPreview> = emptyList()
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState

    private val _filterState = MutableStateFlow(MessagesFilterState())
    val filterState: StateFlow<MessagesFilterState> = _filterState // Change to StateFlow

    private var originalMessages = listOf<ConversationPreview>()
    private var loadedConversations = 0
    private var totalConversations = 0

    init {
        loadMessages()
    }

    fun updateFilter(newState: MessagesFilterState) {
        _filterState.value = newState
    }

    fun clearFilter() {
        _filterState.value = MessagesFilterState()
        _uiState.value = _uiState.value.copy(messages = originalMessages)
    }

    fun applyFilter() {
        val filtered = filterMessages(originalMessages, filterState.value)
        _uiState.value = _uiState.value.copy(messages = filtered)
    }

    private fun loadMessages() {
        _uiState.value = MessagesUiState(isLoading = true)
        val uid = auth.currentUser?.uid ?: return

        db.collection("conversations")
            .whereArrayContains("participants", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = emptyList()
                    )
                    return@addOnSuccessListener
                }

                totalConversations = snapshot.size()
                loadedConversations = 0
                val conversations = mutableListOf<ConversationPreview>()

                snapshot.documents.forEach { doc ->
                    val participants = doc.get("participants") as? List<String> ?: return@forEach

                    // Make sure to get the correct peer ID (not the current user)
                    val peerId = participants.find { it != uid } ?: return@forEach

                    if (peerId.isEmpty()) {
                        loadedConversations++
                        return@forEach
                    }

                    // Load peer info
                    db.collection("users")
                        .document(peerId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (!userDoc.exists()) {
                                loadedConversations++
                                return@addOnSuccessListener
                            }

                            val peer = User(
                                uid = peerId,
                                firstName = userDoc.getString("firstName") ?: "",
                                lastName = userDoc.getString("lastName") ?: "",
                                avatarUrl = userDoc.getString("avatarUrl"),
                                isOnline = userDoc.getBoolean("isOnline") ?: false
                            )

                            // Get last message
                            db.collection("conversations")
                                .document(doc.id)
                                .collection("messages")
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { msgSnap ->
                                    val lastMsg = msgSnap.documents.firstOrNull()
                                    val preview = ConversationPreview(
                                        id = doc.id,
                                        peer = peer,  // This is the conversation partner, not the current user
                                        lastMessage = lastMsg?.getString("text") ?: "",
                                        lastMessageTimestamp = lastMsg?.getLong("timestamp") ?: System.currentTimeMillis(),
                                        timeAgo = formatTimeAgo(lastMsg?.getLong("timestamp") ?: System.currentTimeMillis()),
                                        unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                                    )
                                    conversations.add(preview)

                                    loadedConversations++
                                    if (loadedConversations == totalConversations) {
                                        originalMessages = conversations.sortedByDescending { it.lastMessageTimestamp }
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            messages = originalMessages
                                        )
                                    }
                                }
                                .addOnFailureListener {
                                    loadedConversations++
                                }
                        }
                        .addOnFailureListener {
                            loadedConversations++
                        }
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m"
            diff < 86400_000 -> "${diff / 3600_000}h"
            else -> "${diff / 86400_000}d"
        }
    }
}
