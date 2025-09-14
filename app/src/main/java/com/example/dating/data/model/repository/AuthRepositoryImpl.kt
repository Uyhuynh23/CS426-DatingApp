package com.example.dating.data.model.repository

import android.util.Log
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.data.model.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import okhttp3.OkHttpClient
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

            // Create empty user document in Firestore for email signup
            result.user?.let { firebaseUser ->
                val nameParts = name.split(" ")
                val user = User(
                    uid = firebaseUser.uid,
                    firstName = nameParts.firstOrNull() ?: "",
                    lastName = nameParts.drop(1).joinToString(" "),
                    isOnline = true,
                    lastActive = System.currentTimeMillis()
                )
                db.collection("users").document(firebaseUser.uid).set(user).await()
                Log.d("AuthRepository", "Created new user document for email signup: ${firebaseUser.uid}")
            }

            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signupWithEmailVerification(email: String, password: String): String? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()

            // Create empty user document in Firestore for email verification signup
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    isOnline = true,
                    lastActive = System.currentTimeMillis()
                )
                db.collection("users").document(firebaseUser.uid).set(user).await()
                Log.d("AuthRepository", "Created new user document for email verification signup: ${firebaseUser.uid}")
            }

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

            // Check if user document already exists first to distinguish signup vs login
            result.user?.let { firebaseUser ->
                val docRef = db.collection("users").document(firebaseUser.uid)
                val document = docRef.get().await()

                if (document.exists()) {
                    // User document already exists - this is a login, not signup
                    Log.d("AuthRepository", "User document already exists for Google login: ${firebaseUser.uid}")
                    // Update online status for existing user
                    docRef.update(
                        mapOf(
                            "isOnline" to true,
                            "lastActive" to System.currentTimeMillis()
                        )
                    ).await()
                } else {
                    // Create empty user document for new Google user (minimal data to avoid existing user detection)
                    val user = User(
                        uid = firebaseUser.uid,
                        isOnline = true,
                        lastActive = System.currentTimeMillis()
                        // Intentionally NOT setting firstName, lastName, avatarUrl to keep document truly empty
                        // This prevents SignUpScreen from detecting it as existing user
                    )

                    docRef.set(user).await()
                    Log.d("AuthRepository", "Created new empty user document for Google signup: ${firebaseUser.uid}")
                }
            }

            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signupWithFacebook(token: String): Resource<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val result = firebaseAuth.signInWithCredential(credential).await()

            // Check if user document already exists first to distinguish signup vs login
            result.user?.let { firebaseUser ->
                val docRef = db.collection("users").document(firebaseUser.uid)
                val document = docRef.get().await()

                if (document.exists()) {
                    // User document already exists - this is a login, not signup
                    Log.d("AuthRepository", "User document already exists for Facebook login: ${firebaseUser.uid}")
                    // Update online status for existing user
                    docRef.update(
                        mapOf(
                            "isOnline" to true,
                            "lastActive" to System.currentTimeMillis()
                        )
                    ).await()
                } else {
                    // Create empty user document for new Facebook user (minimal data to avoid existing user detection)
                    val user = User(
                        uid = firebaseUser.uid,
                        isOnline = true,
                        lastActive = System.currentTimeMillis()
                        // Intentionally NOT setting firstName, lastName, avatarUrl to keep document truly empty
                        // This prevents SignUpScreen from detecting it as existing user
                    )

                    docRef.set(user).await()
                    Log.d("AuthRepository", "Created new empty user document for Facebook signup: ${firebaseUser.uid}")
                }
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

    override suspend fun checkIfEmailExists(email: String): List<String> {
        return try {
            val result = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            result.signInMethods ?: emptyList()
        } catch (e: Exception) {
            emptyList() // or rethrow if you want to surface the error
        }
    }
}
