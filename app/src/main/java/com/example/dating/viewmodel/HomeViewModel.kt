package com.example.dating.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.data.model.repository.FavoriteRepository
import com.example.dating.data.model.repository.HomeRepository
import com.example.dating.data.model.repository.RecommendationRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val homeRepository: HomeRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _matchFoundUserId = MutableStateFlow<String?>(null)
    val matchFoundUserId: StateFlow<String?> = _matchFoundUserId

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    private val _profileIndex = MutableStateFlow(0)
    val profileIndex: StateFlow<Int> = _profileIndex

    init {
        fetchHome()
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun fetchHome() {
        _profileIndex.value = 0
        val currentUserId = getCurrentUserId() ?: return
        android.util.Log.d("HomeViewModel", "fetchHome called for uid=$currentUserId")
        viewModelScope.launch {
            try {
                _usersState.value = Resource.Loading
                val profileIds = homeRepository.fetchProfiles().filter { it != currentUserId }
                android.util.Log.d("HomeViewModel", "Fetched profileIds: $profileIds")
                if (profileIds.isEmpty()) {
                    android.util.Log.d("HomeViewModel", "No profiles found for uid=$currentUserId")
                    _usersState.value = Resource.Success(emptyList())
                    return@launch
                }
                val users = homeRepository.getUserProfilesByIds(profileIds)
                android.util.Log.d("HomeViewModel", "Fetched user profiles: $users")
                _usersState.value = Resource.Success(users)
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error in fetchHome", e)
                _usersState.value = Resource.Failure(e)
            }
        }
    }

    fun likeProfile(likedId: String) {
        val likerId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                // Add favorite using repository
                favoriteRepository.addFavorite(likerId, likedId)
                // Check for match using repository
                val isMatch = favoriteRepository.isMatch(likerId, likedId)
                if (isMatch) {
                    _matchFoundUserId.value = likedId
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId")
                } else {
                    _matchFoundUserId.value = null
                    android.util.Log.d("HomeViewModel", "Calling MatchRepository.saveMatch with $likerId and $likedId, status=false")
                }
            } catch (e: Exception) {
                _usersState.value = Resource.Failure(e as? Exception ?: Exception(e.message))
            }
        }
    }

    fun setProfileIndex(index: Int) {
        _profileIndex.value = index
    }

    fun nextProfile() {
        _profileIndex.value++
    }

    fun resetProfileIndex() {
        _profileIndex.value = 0
    }

    fun resetMatchFoundUserId() {
        _matchFoundUserId.value = null
    }

    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun saveUserLocationIfPermitted(
        context: Context,
        locationClient: FusedLocationProviderClient,
        locationPermissionsState: MultiplePermissionsState,
        locationSaved: MutableState<Boolean>
    ) {
        if (locationPermissionsState.allPermissionsGranted && !locationSaved.value) {
            try {
                val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    val uid = getCurrentUserId() ?: return
                    withContext(Dispatchers.IO) {
                        val location = locationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            null
                        ).await()
                        if (location != null) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = try {
                                geocoder.getFromLocation(
                                    location.latitude,
                                    location.longitude,
                                    1
                                )
                            } catch (e: IOException) {
                                emptyList<android.location.Address>()
                            }
                            val city = addresses?.getOrNull(0)?.adminArea.orEmpty()
                            val district = addresses?.getOrNull(0)?.subAdminArea.orEmpty()
                            val userLocation = mapOf(
                                "city" to city,
                                "district" to district,
                                "lat" to location.latitude,
                                "lng" to location.longitude
                            )
                            homeRepository.saveUserLocation(uid, userLocation)
                            locationSaved.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    fun updateEmbeddingWithFeedbackForSwipe(swipedUser: User, liked: Boolean) {
        val likerId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userEmbedding = recommendationRepository.getEmbeddingFromFirestore(likerId)
                val otherEmbedding = recommendationRepository.getEmbeddingFromFirestore(swipedUser.uid)
                if (userEmbedding == null || otherEmbedding == null) {
                    android.util.Log.w(
                        "HomeViewModel",
                        "Embeddings not found for $likerId or ${swipedUser.uid}, skipping update"
                    )
                    return@launch
                }
                recommendationRepository.updateEmbeddingWithFeedback(
                    likerId,
                    userEmbedding,
                    otherEmbedding,
                    liked
                )
                android.util.Log.d(
                    "HomeViewModel",
                    "updateEmbeddingWithFeedback called for ${likerId} and ${swipedUser.uid}, liked=$liked"
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error updating embedding with feedback", e)
            }
        }
    }

}
