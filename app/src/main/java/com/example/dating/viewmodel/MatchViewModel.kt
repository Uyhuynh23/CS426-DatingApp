package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(private val matchRepository: MatchRepository = MatchRepository()) : ViewModel() {
    private val _currentUserFirstName = MutableStateFlow<String>("You")
    val currentUserFirstName: StateFlow<String> = _currentUserFirstName

    fun saveMatch(userId1: String, userId2: String, status: Boolean) {
        viewModelScope.launch {
            matchRepository.saveMatch(userId1, userId2, status)
        }
    }

    fun fetchCurrentUserFirstName(uid: String?) {
        android.util.Log.d("MatchViewModel", "fetchCurrentUserFirstName called with uid=$uid")
        if (uid == null) {
            _currentUserFirstName.value = "You"
            return
        }
        viewModelScope.launch {
            try {
                val firstName = matchRepository.getUserFirstName(uid)
                android.util.Log.d("MatchViewModel", "fetchCurrentUserFirstName result: firstName=$firstName")
                _currentUserFirstName.value = firstName
            } catch (e: Exception) {
                android.util.Log.e("MatchViewModel", "fetchCurrentUserFirstName error: ${e.message}")
                _currentUserFirstName.value = "You"
            }
        }
    }
}
