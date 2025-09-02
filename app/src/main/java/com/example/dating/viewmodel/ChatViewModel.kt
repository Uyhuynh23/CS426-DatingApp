package com.example.dating.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.ChatMessage
import com.example.dating.data.model.DayMessages
import com.example.dating.data.model.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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

    private val _groupedMessages = MutableStateFlow<List<DayMessages>>(emptyList())
    val groupedMessages: StateFlow<List<DayMessages>> = _groupedMessages

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

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesRealtime(conversationId)
                .collect { list ->
                    _groupedMessages.value = groupByDay(list)
                }
        }
    }


    fun sendMessage(conversationId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, text)
        }
    }

    private fun groupByDay(messages: List<ChatMessage>): List<DayMessages> {
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        return messages
            .groupBy { msg ->
                val cal = Calendar.getInstance().apply { timeInMillis = msg.timestamp }
                val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                if (isToday) "Today" else sdf.format(msg.timestamp)
            }
            .map { (label, msgs) -> DayMessages(label, msgs.sortedBy { it.timestamp }) }
            .sortedBy { it.messages.first().timestamp }
    }
}