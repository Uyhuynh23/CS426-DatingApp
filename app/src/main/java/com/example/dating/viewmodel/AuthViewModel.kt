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

    private val _facebookSignInFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val facebookSignInFlow: StateFlow<Resource<FirebaseUser>?> = _facebookSignInFlow

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    // Track last login method
    enum class LoginMethod { NORMAL, GOOGLE, FACEBOOK }
    var lastLoginMethod: LoginMethod? = null
        private set

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading
        val result = repository.login(email, password)
        _loginFlow.value = result
        if (result is Resource.Success) {
            lastLoginMethod = LoginMethod.NORMAL
        }
    }

    fun signupUser(name: String, email: String, password: String) = viewModelScope.launch {
        _signupFlow.value = Resource.Loading
        val result = repository.signup(name, email, password)
        _signupFlow.value = result
        if (result is Resource.Success) {
            lastLoginMethod = LoginMethod.NORMAL
        }
    }

    fun logout(profileViewModel: ProfileViewModel) {
        try {
            repository.logout()
            _loginFlow.value = null
            _signupFlow.value = null
            _googleSignInFlow.value = null
            _facebookSignInFlow.value = null
            lastLoginMethod = null // <-- Clear login method on logout
            profileViewModel.clearUser()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error during logout", e)
        }
        // currentUser?.reload() // Remove reload, not needed after logout
    }

    fun clearGoogleSignInState() {
        _googleSignInFlow.value = null
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
        if (result is Resource.Success) {
            lastLoginMethod = LoginMethod.GOOGLE
        }
    }



    private fun initGoogleSignIn(context: Context): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("227046142854-vuqvpvateg6m6gev12036jovd462t348.apps.googleusercontent.com") // Web client ID
            .requestEmail()
            .requestProfile()
            .build()
    }

    fun performGoogleSignIn(context: Context, onSignInIntent: (android.content.Intent) -> Unit) {
        try {
            val gso = initGoogleSignIn(context)
            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            // Clear any previous sign-in state
            googleSignInClient.signOut().addOnCompleteListener {
                try {
                    val signInIntent = googleSignInClient.signInIntent
                    Log.d("GoogleSignIn", "Launching sign in intent")
                    onSignInIntent(signInIntent)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Error creating sign in intent", e)
                    viewModelScope.launch {
                        _googleSignInFlow.value = Resource.Failure(e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error in performGoogleSignIn", e)
            viewModelScope.launch {
                _googleSignInFlow.value = Resource.Failure(e)
            }
        }
    }

    fun signupWithFacebook(token: String) {
        viewModelScope.launch {
            _facebookSignInFlow.value = Resource.Loading
            val result = repository.signupWithFacebook(token)
            _facebookSignInFlow.value = result
            if (result is Resource.Success) {
                lastLoginMethod = LoginMethod.FACEBOOK
            }
        }
    }

}