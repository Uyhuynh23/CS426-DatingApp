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
                _usersState.value = Resource.Loading

                // Get list of users who liked me
                val likedMeSnapshot = favoriteRepository.getFavoritesByLikedId(currentUserId)
                val likedMeIds = likedMeSnapshot.mapNotNull { it["likerId"] as? String }
                android.util.Log.d("FavoriteViewModel", "likedMeIds: $likedMeIds")

                // If empty, return immediately
                if (likedMeIds.isEmpty()) {
                    _usersState.value = Resource.Success(emptyList())
                    return@launch
                }

                // Fetch user profiles
                val users = getUserProfilesByIds(likedMeIds)
                _usersState.value = Resource.Success(users)
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e)
            }
        }
    }

    private suspend fun getUserProfilesByIds(uids: List<String>): List<User> {
        return try {
            val snapshot = db.collection("users")
                .whereIn("uid", uids)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            android.util.Log.e("FavoriteViewModel", "Error fetching users: ", e)
            emptyList()
        }
    }
}
