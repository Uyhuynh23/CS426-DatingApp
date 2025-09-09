package com.example.dating.data.model.repository

import com.example.dating.data.model.Resource
import com.example.dating.data.model.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {


    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // Set user online status after login
            result.user?.let { user ->
                val uid = user.uid
                    db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "isOnline" to true,
                            "lastActive" to System.currentTimeMillis()
                        )
                    ).await()
            }
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
            return Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signupWithEmailVerification(email: String, password: String): String? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()
            null // null means success
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "Failed to sign up or send verification email"
        }
    }

    override suspend fun signupWithGoogle(idToken: String): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signupWithFacebook(accessToken: String): Resource<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = firebaseAuth.signInWithCredential(credential).await()

            // (Optional) Initialize/update user profile doc if needed
            result.user?.let { user ->
                val uid = user.uid
                db.collection("users").document(uid)
                    .set(
                        mapOf(
                            "uid" to uid,
                            "firstName" to (user.displayName?.substringBefore(" ") ?: ""),
                            "lastName"  to (user.displayName?.substringAfter(" ") ?: ""),
                            "avatarUrl" to (user.photoUrl?.toString() ?: ""),
                            "isOnline"  to true,
                            "lastActive" to System.currentTimeMillis()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
            }

            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override fun logout() {
        // Set user offline status before logout
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "isOnline" to false,
                        "lastActive" to System.currentTimeMillis()
                    )
                )
        }
        firebaseAuth.signOut()
    }

}
