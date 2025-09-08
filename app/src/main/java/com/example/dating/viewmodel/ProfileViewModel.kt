package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.data.model.UserFilterPreferences
import com.example.dating.data.model.repository.AuthRepository
import com.example.dating.data.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: AuthRepository,
    private val recommendationRepository: com.example.dating.data.model.repository.RecommendationRepository // Injected instance
    ) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

    // ======= Gallery =======
    private val _galleryUploading = MutableStateFlow(false)
    val galleryUploading = _galleryUploading.asStateFlow()

    private val _galleryError = MutableStateFlow<String?>(null)
    val galleryError = _galleryError.asStateFlow()

    private fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

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


    fun updateProfile(user: User) {
        android.util.Log.d("ProfileViewModel", "updateProfile called with: $user")
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.updateUser(user)
            _updateState.value = result
            if (result is Resource.Success) {
                _user.value = user
                try {
                    // Use injected RecommendationRepository instead of creating a new instance
                    recommendationRepository.createEmbedding(user)
                } catch (e: Exception) {
                    android.util.Log.e("ProfileViewModel", "Failed to update embedding: ${e.message}")
                }
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

    fun createUser(user: User) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.createUser(user)
            _updateState.value = result
            if (result is Resource.Success) {
                _user.value = user
            }
        }
    }

    fun uploadAvatar(uri: android.net.Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storage = FirebaseStorage.getInstance()
        val avatarRef = storage.reference.child("avatars/$uid.jpg")
        avatarRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot: com.google.firebase.storage.UploadTask.TaskSnapshot ->
                avatarRef.downloadUrl.addOnSuccessListener { downloadUrl: android.net.Uri ->
                    viewModelScope.launch {
                        userRepository.updateAvatarUrl(uid, downloadUrl.toString())
                        android.util.Log.d("ProfileViewModel", "Avatar updated: $downloadUrl")
                    }
                }
            }
            .addOnFailureListener { e: Exception ->
                android.util.Log.e("ProfileViewModel", "Failed to upload avatar: ${e.message}")
            }
    }


    fun updateFilterPreferences(uid: String, prefs: UserFilterPreferences, onResult: (Resource<Unit>) -> Unit) {
        android.util.Log.d("UserViewModel", "updateFilterPreferences called: uid=$uid, prefs=$prefs")
        viewModelScope.launch {
            val result = userRepository.updateFilterPreferences(uid, prefs)
            android.util.Log.d("UserViewModel", "updateFilterPreferences result: $result for uid=$uid")
            onResult(result)
        }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // ======= Max 5 images =======
    fun addGalleryImage(imageUri: Uri) {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            _galleryError.value = null
            _galleryUploading.value = true
            try {
                val current = user.value?.imageUrl ?: emptyList()
                if (current.size >= 5) {
                    _galleryError.value = "You can upload up to 5 photos."
                    return@launch
                }
                val url = userRepository.uploadGalleryImage(uid, imageUri)
                userRepository.addGalleryUrl(uid, url)
            } catch (e: Exception) {
                _galleryError.value = e.localizedMessage ?: "Upload failed."
            } finally {
                _galleryUploading.value = false
            }
        }
    }

    // ======= THÊM HÀM: Xoá ảnh khỏi gallery =======
    fun removeGalleryImage(url: String) {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            try {
                userRepository.removeGalleryUrl(uid, url)
            } catch (e: Exception) {
                _galleryError.value = e.localizedMessage ?: "Delete failed."
            }
        }
    }

}
