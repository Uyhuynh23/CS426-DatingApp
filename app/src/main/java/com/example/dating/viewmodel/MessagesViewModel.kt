package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.MessagesFilterState
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.repository.FirebaseMessagesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.dating.data.model.filterMessages
import android.util.Log

data class MessagesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ConversationPreview> = emptyList()
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: FirebaseMessagesRepository
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

    private fun loadMessages() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getConversations(currentUid).collect { conversations ->
                originalMessages = conversations.sortedByDescending { it.lastMessageTimestamp }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = originalMessages
                )
                Log.d("MessagesViewModel", "Conversations updated, count=${conversations.size}")
            }
        }
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
