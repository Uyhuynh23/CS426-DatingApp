package com.example.dating.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.ChatMessage
import com.example.dating.data.model.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _peerName = MutableStateFlow("")
    val peerName: StateFlow<String> = _peerName

    private val _peerAvatar = MutableStateFlow("")
    val peerAvatar: StateFlow<String> = _peerAvatar

    private val chatRepository = ChatRepository(db, auth)

    fun loadPeer(conversationId: String) {
        viewModelScope.launch {
            val (name, photoUrl) = chatRepository.getPeerInfo(conversationId)
            _peerName.value = name
            _peerAvatar.value = photoUrl
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesRealtime(conversationId)
                .collect { list ->
                    _messages.value = list
                }
        }
    }


    fun sendMessage(conversationId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, text)
        }
    }
}