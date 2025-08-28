package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dating.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var registration: ListenerRegistration? = null

    init {
        loadCurrentUser()
    }

    /** Public: gọi lại sau khi login/logout để reload dữ liệu */
    fun refresh() = loadCurrentUser()

    private fun loadCurrentUser() {
        // hủy listener cũ nếu có
        registration?.remove()
        _isLoading.value = true

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            // Chưa đăng nhập → cho dummy để UI chạy
            _user.value = createDummyUser()
            _isLoading.value = false
            return
        }

        // Realtime listen users/{uid}
        registration = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _user.value = createDummyUser()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _user.value = snapshot.toObject(User::class.java) ?: createDummyUser()
                } else {
                    _user.value = createDummyUser()
                }
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }

    private fun createDummyUser(): User {
        val demoImages = listOf(
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9",
            "https://images.unsplash.com/photo-1512436991641-6745cdb1723f"
        )
        return User(
            uid = "dummy",
            firstName = "Jessica",
            lastName = "Parker",
            birthday = "1995-06-15",
            job = "Professional model",
            location = "Chicago, IL, United States",
            description = "My name is Jessica Parker and I enjoy meeting new people and finding ways to help them have an uplifting experience.",
            interests = listOf("Traveling", "Books", "Music", "Dancing", "Modeling"),
            imageUrl = demoImages,
            avatarUrl = demoImages.first()
        )
    }
}
