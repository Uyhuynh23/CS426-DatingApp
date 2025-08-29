package com.example.dating.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource


class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
)  {

    fun getUser(uid: String): Flow<User?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        android.util.Log.d("UserRepository", "getUser: docRef=$docRef " +
                "for uid=$uid")
        val listener = docRef.addSnapshotListener { snapshot, _ ->
            val user = snapshot?.toObject(User::class.java)
            // Ensure uid and imageUrl are set from document id and default if missing
            val userWithUid = user?.copy(
                uid = snapshot.id,
                imageUrl = user.imageUrl ?: emptyList()
            )
            android.util.Log.d("UserRepository", "getUser: uid=$uid, user=$userWithUid")
            trySend(userWithUid)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            android.util.Log.d("ProfileViewModel", "updateProfile called with: $user")
            firestore.collection("users").document(user.uid).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    suspend fun updateGender(uid: String, gender: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(uid).update("gender", gender).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    suspend fun updateInterests(uid: String, interests: List<String>): Resource<Unit> {
        return try {
            firestore.collection("users").document(uid).update("interests", interests).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    suspend fun updateJobLocationDescription(uid: String, job: String?, location: String?, description: String?): Resource<Unit> {
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "job" to job,
                    "location" to location,
                    "description" to description
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    suspend fun uploadAvatar(uid: String, uri: android.net.Uri): String {
        // TODO: Implement Firebase Storage upload and return the URL
        return ""
    }

    suspend fun createUser(user: User): Resource<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }
}
