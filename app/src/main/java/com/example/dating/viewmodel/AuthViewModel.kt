package com.example.dating.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.Resource
import com.example.dating.data.model.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Resource<FirebaseUser>?> = _loginFlow

    private val _signupFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signupFlow: StateFlow<Resource<FirebaseUser>?> = _signupFlow

    private val _googleSignInFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val googleSignInFlow: StateFlow<Resource<FirebaseUser>?> = _googleSignInFlow

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    fun signupUser(name: String, email: String, password: String) = viewModelScope.launch {
        _signupFlow.value = Resource.Loading
        val result = repository.signup(name, email, password)
        _signupFlow.value = result
    }

    fun logout() {
        repository.logout()
        _loginFlow.value = null
        _signupFlow.value = null
    }

    suspend fun signupUserWithEmailVerification(email: String, password: String): String? {
        val result = repository.signupWithEmailVerification(email, password)
        return result
    }

    suspend fun checkEmailVerified(): Boolean {
        val user = repository.currentUser
        user?.reload()?.await()
        return user?.isEmailVerified == true
    }

    fun signupWithGoogle(idToken: String) = viewModelScope.launch {
        _googleSignInFlow.value = Resource.Loading
        val result = repository.signupWithGoogle(idToken)
        _googleSignInFlow.value = result
    }

    fun initGoogleSignIn(context: Context): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken("224972776925-jcghkv22uojd0ka97ag7ebqn8rkuu0gl.apps.googleusercontent.com")
            .build()
    }

    fun performGoogleSignIn(context: Context, onSignInIntent: (android.content.Intent) -> Unit) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("224972776925-jcghkv22uojd0ka97ag7ebqn8rkuu0gl.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            // Clear any previous sign-in and start fresh
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    onSignInIntent(signInIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error creating sign-in intent: ${e.message}")
            viewModelScope.launch {
                _googleSignInFlow.value = Resource.Failure(e)
            }
        }
    }

}