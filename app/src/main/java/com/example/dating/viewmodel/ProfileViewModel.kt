package com.example.dating.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveProfile(
        firstName: String,
        lastName: String,
        birthday: String?,
        imageUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "birthday" to birthday,
            "imageUrl" to imageUrl
        )
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun updateGender(
        gender: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .update("gender", gender)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun updateInterests(
        interests: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .update("interests", interests)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun updateJobLocationDescription(
        job: String?,
        location: String?,
        description: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .update(mapOf(
                    "job" to job,
                    "location" to location,
                    "description" to description
                ))
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }
}
