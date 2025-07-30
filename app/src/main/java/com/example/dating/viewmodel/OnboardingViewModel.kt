package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _onboardingState = MutableStateFlow(false)
    val onboardingState: StateFlow<Boolean> = _onboardingState

    init {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            _onboardingState.value = document.getBoolean("onboardingCompleted") ?: false
                        }
                    }
            }
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .update("onboardingCompleted", true)
                    .addOnSuccessListener {
                        _onboardingState.value = true
                    }
            }
        }
    }
}

