package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.AuthRepository
import com.example.dating.data.model.repository.UserRepository
import com.example.dating.data.model.Resource

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: AuthRepository,
    ) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                userRepository.getUser(uid).collect { fetchedUser ->
                    android.util.Log.d("ProfileViewModel", "Fetched user: $fetchedUser")
                    _user.value = fetchedUser
                }
            }
        } else {
            android.util.Log.w("ProfileViewModel", "No UID found in AuthRepository")
        }
    }

    fun loadUser(uid: String) {
        viewModelScope.launch {
            userRepository.getUser(uid).collect { _user.value = it }
        }
    }

    fun updateProfile(user: User) {
        android.util.Log.d("ProfileViewModel", "updateProfile called with: $user")
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.updateUser(user)
            _updateState.value = result
            if (result is Resource.Success) {
                _user.value = user
            }
        }
    }

    fun updateGender(gender: String) {
        val currentUser = _user.value ?: return
        val updatedUser = currentUser.copy(gender = gender)
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.updateGender(updatedUser.uid, gender)
            _updateState.value = result
            if (result is Resource.Success) {
                _user.value = updatedUser
            }
        }
    }

    fun updateInterests(interests: List<String>) {
        val currentUser = _user.value ?: return
        val updatedUser = currentUser.copy(interests = interests)
        android.util.Log.w("ProfileViewModel", currentUser.toString())

        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.updateInterests(updatedUser.uid, interests)
            _updateState.value = result
            if (result is Resource.Success) {
                _user.value = updatedUser
            }
        }
    }

    fun updateJobLocationDescription(job: String?, location: String?, description: String?) {
        val currentUser = _user.value ?: return
        val updatedUser = currentUser.copy(job = job, location = location, description = description)
        viewModelScope.launch {
            userRepository.updateJobLocationDescription(updatedUser.uid, job, location, description)
            _user.value = updatedUser
        }
    }
}
