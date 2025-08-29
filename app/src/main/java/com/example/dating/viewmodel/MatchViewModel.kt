package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {
    private val _currentUserFirstName = MutableStateFlow<String>("You")
    val currentUserFirstName: StateFlow<String> = _currentUserFirstName

    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl

    data class UserInfo(val uid: String, val firstName: String, val avatarUrl: String?)

    private val _userInfo = MutableStateFlow<List<UserInfo>>(emptyList())
    val userInfo: StateFlow<List<UserInfo>> = _userInfo

    fun saveMatch(userId1: String, userId2: String, status: Boolean) {
        viewModelScope.launch {
            matchRepository.saveMatch(userId1, userId2, status)
        }
    }

    fun fetchUsersInfo(uids: List<String>) {
        viewModelScope.launch {
            try {
                val userInfoList = uids.mapNotNull { userId ->
                    val userDoc = matchRepository.getUserDocument(userId)
                    val firstName = userDoc["firstName"] as? String ?: "You"
                    val avatarUrl = userDoc["avatarUrl"] as? String
                    UserInfo(uid = userId, firstName = firstName, avatarUrl = avatarUrl)
                }
                _userInfo.value = userInfoList
            } catch (e: Exception) {
                _userInfo.value = emptyList()
            }
        }
    }
}
