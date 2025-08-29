package com.example.dating.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.repository.MatchRepository
import com.example.dating.data.model.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val favoriteRepository: FavoriteRepository,
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _profilesState = MutableStateFlow<Resource<List<String>>>(Resource.Loading)
    val profilesState: StateFlow<Resource<List<String>>> = _profilesState

    private val _matchFoundUserId = MutableStateFlow<String?>(null)
    val matchFoundUserId: StateFlow<String?> = _matchFoundUserId

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    init {

        fetchHome()
    }

    fun fetchHome() {
        viewModelScope.launch {
            _profilesState.value = Resource.Loading
            try {
                val result = homeRepository.fetchProfiles() // Should return List<String> (UIDs)
                _profilesState.value = Resource.Success(result)
            } catch (e: Exception) {
                _profilesState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun likeProfile(likedId: String) {
        val likerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Add favorite using repository
                favoriteRepository.addFavorite(likerId, likedId)
                // Check for match using repository
                val isMatch = favoriteRepository.isMatch(likerId, likedId)
                if (isMatch) {
                    _matchFoundUserId.value = likedId
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId")
                    matchRepository.saveMatch(likerId, likedId, true)
                } else {
                    _matchFoundUserId.value = null
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId, status=false")
                    matchRepository.saveMatch(likerId, likedId, false)
                }
            } catch (e: Exception) {
                _profilesState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun getUserProfilesByIds(uids: List<String>) {
        viewModelScope.launch {
            _usersState.value = Resource.Loading
            try {
                val users = homeRepository.getUserProfilesByIds(uids)
                _usersState.value = Resource.Success(users)
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun fetchUserById(id: String) {
        viewModelScope.launch {
            _usersState.value = Resource.Loading
            try {
                val user = homeRepository.getUserProfilesByIds(listOf(id)).firstOrNull()
                if (user != null) {
                    _usersState.value = Resource.Success(listOf(user))
                } else {
                    _usersState.value = Resource.Failure(Exception("User not found"))
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
