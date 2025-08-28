package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.repository.MatchRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val matchRepository = MatchRepository()
    private val favoriteRepository = FavoriteRepository()
   
    private val _profiles = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val profiles: StateFlow<List<Map<String, Any>>> = _profiles

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _matchFoundUserId = MutableStateFlow<String?>(null)
    val matchFoundUserId: StateFlow<String?> = _matchFoundUserId

    fun fetchHome() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("users").get().await()
                val allProfiles = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (doc.id != currentUserId && data != null) data + ("uid" to doc.id) else null
                }
                _profiles.value = allProfiles
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
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
                _errorMessage.value = e.message
            }
        }
    }

    fun resetMatchFoundUserId() {
        _matchFoundUserId.value = null
    }
}
