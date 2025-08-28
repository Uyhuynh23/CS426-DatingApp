package com.example.dating.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.repository.FirebaseMessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repo: FirebaseMessagesRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ConversationPreview>>(emptyList())
    val messages: StateFlow<List<ConversationPreview>> = _messages

    init {
        viewModelScope.launch {
            repo.getConversations().collect {
                _messages.value = it
            }
        }
    }
}
