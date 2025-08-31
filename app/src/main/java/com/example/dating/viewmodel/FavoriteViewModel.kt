package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _likedMeProfiles = MutableStateFlow<Resource<List<String>>>(Resource.Loading)
    val likedMeProfiles: StateFlow<Resource<List<String>>> = _likedMeProfiles

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    init {
        fetchFavorites()
    }

    fun fetchFavorites() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        android.util.Log.d("FavoriteViewModel", "currentUserId: $currentUserId")

        viewModelScope.launch {
            try {
                _likedMeProfiles.value = Resource.Loading
                _usersState.value = Resource.Loading

                // Get all users who liked me
                val likedMeSnapshot = favoriteRepository.getFavoritesByLikedId(currentUserId)
                val likedMeIds = likedMeSnapshot.mapNotNull { it["likerId"] as? String }
                android.util.Log.d("FavoriteViewModel", "likedMeIds: $likedMeIds")

                if (likedMeIds.isEmpty()) {
                    _likedMeProfiles.value = Resource.Success(emptyList())
                    _usersState.value = Resource.Success(emptyList())
                    return@launch
                }

                // Filter out users that I also liked (matches) from the list
                val nonMatchedIds = mutableListOf<String>()

                for (likerId in likedMeIds) {
                    // Check if I liked them back (if yes, it's a match)
                    val isMatch = favoriteRepository.isMatch(currentUserId, likerId)

                    // Only include users that are not matches
                    if (!isMatch) {
                        nonMatchedIds.add(likerId)
                    }
                }

                _likedMeProfiles.value = Resource.Success(nonMatchedIds)

                // If no non-matched users, return empty list
                if (nonMatchedIds.isEmpty()) {
                    _usersState.value = Resource.Success(emptyList())
                } else {
                    // Get profiles for non-matched users
                    getUserProfilesByIds(nonMatchedIds)
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoriteViewModel", "Error fetching favorites", e)
                _likedMeProfiles.value = Resource.Failure(e as? Exception ?: Exception(e.message))
                _usersState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun deleteFavorite(likedId: String) {
        val likerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                favoriteRepository.deleteFavorite(likerId, likedId)
                fetchFavorites() // Refresh after delete
            } catch (_: Exception) {}
        }
    }

    fun getUserProfilesByIds(uids: List<String>) {
        viewModelScope.launch {
            _usersState.value = Resource.Loading
            try {
                val users = favoriteRepository.getUserProfilesByIds(uids)
                _usersState.value = Resource.Success(users)
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }
}
