package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.UserRepository
import kotlinx.coroutines.flow.first
import com.example.dating.data.model.repository.FirebaseMessagesRepository

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val messagesRepository: FirebaseMessagesRepository
) : ViewModel() {

    data class UserInfo(val uid: String, val firstName: String, val avatarUrl: String?)

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _matchedUserInfo = MutableStateFlow<UserInfo?>(null)
    val matchedUserInfo: StateFlow<UserInfo?> = _matchedUserInfo

    fun setUser(user: User?) {
        _user.value = user
        if (user != null) {
            _userInfo.value = UserInfo(
                uid = user.uid,
                firstName = user.firstName,
                avatarUrl = user.imageUrl.firstOrNull()
            )
        } else {
            _userInfo.value = null
        }
    }

    fun setMatchedUser(user: User?) {
        if (user != null) {
            _matchedUserInfo.value = UserInfo(
                uid = user.uid,
                firstName = user.firstName,
                avatarUrl = user.imageUrl.firstOrNull()
            )
        } else {
            _matchedUserInfo.value = null
        }
    }

    fun saveMatch(userId1: String, userId2: String, status: Boolean) {
        viewModelScope.launch {
            matchRepository.saveMatch(userId1, userId2, status)
            if (status) {
                messagesRepository.createConversation(userId1, userId2)
            }
        }
    }

    suspend fun getUserById(id: String): User? {
        return userRepository.getUser(id).first()
    }
}
