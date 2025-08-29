package com.example.dating.data.model.repository

import com.example.dating.data.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser>
    suspend fun signupWithGoogle(idToken: String): Resource<FirebaseUser>
    fun logout()
    suspend fun signupWithEmailVerification(email: String, password: String): String?
}