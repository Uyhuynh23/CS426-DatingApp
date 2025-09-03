package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

    @Inject lateinit var userRepository: UserRepository

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var registration: ListenerRegistration? = null

    /**
     * Gọi từ UserProfileScreen. Nếu uid=null -> lấy current user;
     * nếu vẫn null -> fallback dummy để test UI.
     */
    fun observeUser(uid: String?) {
        registration?.remove()
        _isLoading.value = true

        val targetUid = uid ?: auth.currentUser?.uid
        if (targetUid.isNullOrBlank()) {
            _user.value = createDummyUser()
            _isLoading.value = false
            return
        }

        registration = firestore.collection("users")
            .document(targetUid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) {
                    _user.value = createDummyUser()
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                _user.value = snapshotToUser(snap, targetUid)
                _isLoading.value = false
            }
    }

    suspend fun isUserOnline(uid: String): Boolean {
        return userRepository.isUserOnline(uid)
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }

    // ---- Helper: đọc an toàn, lọc null ----
    private fun snapshotToUser(doc: DocumentSnapshot, uid: String): User {
        val imageUrl = (doc.get("imageUrl") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val interests = (doc.get("interests") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        return User(
            uid = uid,
            firstName = doc.getString("firstName").orEmpty(),
            lastName  = doc.getString("lastName").orEmpty(),
            birthday  = doc.getString("birthday"),
            imageUrl  = imageUrl,
            avatarUrl = doc.getString("avatarUrl") ?: imageUrl.firstOrNull(),
            gender    = doc.getString("gender"),
            job       = doc.getString("job") ?: "Professional model",
            location  = doc.getString("location") ?: "Chicago, IL, United States",
            description = doc.getString("description"),
            interests   = interests
        )
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
            imageUrl = demoImages,
            avatarUrl = demoImages.first(),
            job = "Professional model",
            location = "Chicago, IL, United States",
            description = "My name is Jessica Parker and I enjoy meeting new people and finding ways to help them have an uplifting experience.",
            interests = listOf("Traveling", "Books", "Music", "Dancing", "Modeling")
        )
    }
}
