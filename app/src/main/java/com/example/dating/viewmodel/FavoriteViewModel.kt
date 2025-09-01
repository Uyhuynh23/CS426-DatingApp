package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModelScope.launch {
                favoriteRepository.listenFavorites(currentUserId).collect { users ->
                    _usersState.value = Resource.Success(users)
                }
            }
        } else {
            _usersState.value = Resource.Failure(Exception("User not authenticated"))
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
