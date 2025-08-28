package com.example.dating.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.FirebaseMessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val messages: List<ConversationPreview> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repo: FirebaseMessagesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState

    init {
        loadFakeMessages()
    }

    private fun loadFakeMessages() {
        viewModelScope.launch {
            delay(1000)

            val fakeUsers = listOf(
                User(
                    uid = "1",
                    firstName = "Jungkook",
                    lastName = "BTS",
                    avatarUrl = "https://shorturl.at/bexN0"  // Jungkook Calvin Klein
                ),
                User(
                    uid = "2",
                    firstName = "Jennie",
                    lastName = "Blackpink",
                    avatarUrl = "https://shorturl.at/hkDJ7"  // Jennie Chanel
                ),
                User(
                    uid = "3",
                    firstName = "Jisoo",
                    lastName = "Blackpink",
                    avatarUrl = "https://shorturl.at/ruxBU"  // Jisoo Dior
                ),
                User(
                    uid = "4",
                    firstName = "Hanni",
                    lastName = "NewJeans",
                    avatarUrl = "https://shorturl.at/ehmGU"  // Hanni NewJeans
                ),
                User(
                    uid = "5",
                    firstName = "IU",
                    lastName = "",
                    avatarUrl = "https://shorturl.at/jvFNX"  // IU
                )
            )

            val fakeMessages = listOf(
                ConversationPreview(
                    peer = fakeUsers[0],
                    lastMessage = "Seven billion people in the world~🎵",
                    timeAgo = "2m",
                    unreadCount = 3,
                    isTyping = false
                ),
                ConversationPreview(
                    peer = fakeUsers[1],
                    lastMessage = "Born Pink World Tour! 🎤",
                    timeAgo = "15m",
                    unreadCount = 0,
                    isTyping = true
                ),
                ConversationPreview(
                    peer = fakeUsers[2],
                    lastMessage = "Flower MV hit 500M views! 🌸",
                    timeAgo = "1h",
                    unreadCount = 1,
                    isTyping = false
                ),
                ConversationPreview(
                    peer = fakeUsers[3],
                    lastMessage = "Super Shy~ Super Shy~ 🎵",
                    timeAgo = "2h",
                    unreadCount = 0,
                    isTyping = false
                ),
                ConversationPreview(
                    peer = fakeUsers[4],
                    lastMessage = "Celebrity new album! ⭐",
                    timeAgo = "1d",
                    unreadCount = 5,
                    isTyping = false
                )
            )

            _uiState.value = MessagesUiState(
                messages = fakeMessages,
                isLoading = false
            )
        }
    }
}
