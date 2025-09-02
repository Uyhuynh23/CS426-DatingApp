package com.example.dating.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _matchFoundUserId = MutableStateFlow<String?>(null)
    val matchFoundUserId: StateFlow<String?> = _matchFoundUserId

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    init {
        fetchHome()
    }

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun fetchHome() {
        val currentUserId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                _usersState.value = Resource.Loading
                // Get list of user IDs for home (excluding current user)
                val profileIds = homeRepository.fetchProfiles().filter { it != currentUserId }
                if (profileIds.isEmpty()) {
                    _usersState.value = Resource.Success(emptyList())
                    return@launch
                }
                // Fetch user profiles
                val users = homeRepository.getUserProfilesByIds(profileIds)
                _usersState.value = Resource.Success(users)
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e)
            }
        }
    }

    fun likeProfile(likedId: String) {
        val likerId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                // Add favorite using repository
                favoriteRepository.addFavorite(likerId, likedId)
                // Check for match using repository
                val isMatch = favoriteRepository.isMatch(likerId, likedId)
                if (isMatch) {
                    _matchFoundUserId.value = likedId
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId")
                } else {
                    _matchFoundUserId.value = null
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId, status=false")
                }
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun resetMatchFoundUserId() {
        _matchFoundUserId.value = null
    }
}
