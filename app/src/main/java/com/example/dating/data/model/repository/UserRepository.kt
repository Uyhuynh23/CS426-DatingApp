package com.example.dating.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import com.example.dating.data.model.UserFilterPreferences
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID


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
        val ref = FirebaseStorage.getInstance()
            .reference.child("users/$uid/avatar/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createUser(user: User): Resource<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    suspend fun updateAvatarUrl(uid: String, avatarUrl: String) {
        firestore.collection("users").document(uid)
            .update("avatarUrl", avatarUrl)
            .await()
    }

    suspend fun updateFilterPreferences(uid: String, prefs: UserFilterPreferences): Resource<Unit> {
        return try {
            android.util.Log.d("UserRepository", "updateFilterPreferences: uid=$uid, prefs=$prefs")
            firestore.collection("users").document(uid).update("filterPreferences", prefs).await()
            android.util.Log.d("UserRepository", "updateFilterPreferences: success for uid=$uid")
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "updateFilterPreferences: failure for uid=$uid", e)
            Resource.Failure(e)
        }
    }

    suspend fun setUserOnlineStatus(uid: String, isOnline: Boolean) {
        firestore.collection("users").document(uid).update(
            mapOf(
                "isOnline" to isOnline,
                "lastActive" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun isUserOnline(uid: String): Boolean {
        val doc = firestore.collection("users").document(uid).get().await()
        val online = doc.getBoolean("isOnline") ?: false
        val lastActive = doc.getLong("lastActive") ?: 0L
        // Consider online if lastActive within 2 minutes
        return online && (System.currentTimeMillis() - lastActive < 2 * 60 * 1000)
    }

    // ====== GALLERY APIS ======
    suspend fun uploadGalleryImage(uid: String, uri: Uri): String {
        val ref = FirebaseStorage.getInstance()
            .reference.child("users/$uid/gallery/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun addGalleryUrl(uid: String, url: String) {
        firestore.collection("users").document(uid)
            .update("imageUrl", FieldValue.arrayUnion(url))
            .await()
    }

    suspend fun removeGalleryUrl(uid: String, url: String) {
        firestore.collection("users").document(uid)
            .update("imageUrl", FieldValue.arrayRemove(url))
            .await()
    }

    suspend fun setGallery(uid: String, urls: List<String>) {
        firestore.collection("users").document(uid)
            .update("imageUrl", urls)
            .await()
    }

}
