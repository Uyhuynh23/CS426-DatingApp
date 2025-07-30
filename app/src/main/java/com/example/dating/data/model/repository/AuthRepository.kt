package com.example.dating.data.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun signInWithCredential(credential: PhoneAuthCredential, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
    // Add other auth methods as needed
}