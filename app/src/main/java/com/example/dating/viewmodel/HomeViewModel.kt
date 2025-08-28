package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _addFavoriteState = MutableStateFlow<Result<Unit>?>(null)
    val addFavoriteState: StateFlow<Result<Unit>?> = _addFavoriteState

    private val _profiles = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val profiles: StateFlow<List<Map<String, Any>>> = _profiles

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addFavorite(likedId: String) {
        val likerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favorite = hashMapOf(
            "likerId" to likerId,
            "likedId" to likedId,
            "timestamp" to Timestamp.now()
        )
        viewModelScope.launch {
            try {
                // Check for duplicate before adding
                val query = db.collection("favorites")
                    .whereEqualTo("likerId", likerId)
                    .whereEqualTo("likedId", likedId)
                    .get().await()
                if (query.isEmpty) {
                    db.collection("favorites").add(favorite)
                    _addFavoriteState.value = Result.success(Unit)
                } else {
                    _addFavoriteState.value = Result.failure(Exception("Already liked"))
                }
            } catch (e: Exception) {
                _addFavoriteState.value = Result.failure(e)
            }
        }
    }

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
}
