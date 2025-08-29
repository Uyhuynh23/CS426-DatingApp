package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.repository.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoriteViewModel(private val favoriteRepository: FavoriteRepository = FavoriteRepository()) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _likedMeProfiles = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val likedMeProfiles: StateFlow<List<Map<String, Any>>> = _likedMeProfiles
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchFavorites() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Users who liked me
                val likedMeSnapshot = favoriteRepository.getFavoritesByLikedId(currentUserId)
                val likedMeIds = likedMeSnapshot.mapNotNull { it["likerId"] as? String }
                android.util.Log.d("FavoriteViewModel", "likedMeIds: $likedMeIds")
                val likedMeProfilesList = favoriteRepository.getUserProfilesByIds(likedMeIds)
                _likedMeProfiles.value = likedMeProfilesList

                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
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
}
