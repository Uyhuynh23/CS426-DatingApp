package com.example.dating.data.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.dating.data.model.User
import javax.inject.Singleton
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.CollectionReference
@Singleton
class HomeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun fetchProfiles(): List<String> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) return emptyList()
        val currentUserDoc = db.collection("users").document(currentUserId).get().await()
        val prefsMap = currentUserDoc.get("filterPreferences") as? Map<*, *>
        val filterPrefs = prefsMap?.let {
            com.example.dating.data.model.UserFilterPreferences(
                preferredGender = it["preferredGender"] as? String,
                minAge = (it["minAge"] as? Long)?.toInt(),
                maxAge = (it["maxAge"] as? Long)?.toInt(),
                maxDistance = (it["maxDistance"] as? Long)?.toInt()
            )
        }
        val snapshot = db.collection("users").get().await()
        val currentUserLocation = currentUserDoc.getString("location")
        val currentUserBirthday = currentUserDoc.getString("birthday")
        val currentUserLatLng = null // TODO: parse location if you use lat/lng
        val uids = snapshot.documents.mapNotNull { doc ->
            val uid = doc.id
            if (uid == currentUserId) return@mapNotNull null
            val gender = doc.getString("gender")
            val birthday = doc.getString("birthday")
            val location = doc.getString("location")
            val distance = (doc.get("distance") as? Long)?.toInt()
            var filterPassed = true
            val infoLog = StringBuilder("UID=$uid, gender=$gender, birthday=$birthday, location=$location, distance=$distance")
            // Filter by gender
            if (filterPrefs?.preferredGender != null && filterPrefs.preferredGender != "Both" && gender != filterPrefs.preferredGender) {
                infoLog.append(" | Gender filter failed")
                filterPassed = false
            }
            // Filter by age
            if (filterPrefs?.minAge != null && filterPrefs?.maxAge != null && birthday != null) {
                var age: Int? = null
                var parseError: String? = null
                try {
                    // Try user-provided logic first
                    age = try {
                        val year = birthday.split("/").getOrNull(2)?.toInt()
                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        if (year != null) currentYear - year else null
                    } catch (e: Exception) { null }
                    // If failed, try other formats
                    if (age == null) {
                        val formats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy")
                        for (fmt in formats) {
                            try {
                                val sdf = java.text.SimpleDateFormat(fmt)
                                val date = sdf.parse(birthday)
                                if (date != null) {
                                    val dobCal = java.util.Calendar.getInstance()
                                    dobCal.time = date
                                    val birthYear = dobCal.get(java.util.Calendar.YEAR)
                                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                    age = currentYear - birthYear
                                    break
                                }
                            } catch (e: Exception) { parseError = e.message }
                        }
                    }
                } catch (e: Exception) {
                    parseError = e.message
                }
                if (age == null || age < filterPrefs.minAge || age > filterPrefs.maxAge) {
                    infoLog.append(" | Age filter failed (age=$age, birthday=$birthday, error=$parseError)")
                    filterPassed = false
                } else {
                    infoLog.append(" | Age=$age")
                }
            }
            // Filter by distance
            if (filterPrefs?.maxDistance != null && distance != null && distance > filterPrefs.maxDistance) {
                infoLog.append(" | Distance filter failed")
                filterPassed = false
            }
            infoLog.append(" | Filter result: ${if (filterPassed) "PASS" else "FAIL"}")
            android.util.Log.d("HomeRepository", infoLog.toString())
            if (filterPassed) uid else null
        }
        android.util.Log.d("HomeRepository", "Filtered UIDs: $uids")
        return uids
    }

    suspend fun getUserProfilesByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        val batchSize = 30
        val batches = userIds.chunked(batchSize)
        val allUsers = mutableListOf<User>()
        for (batch in batches) {
            val snapshot = db.collection("users")
                .whereIn(FieldPath.documentId(), batch)
                .get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                User(
                    uid = doc.id,
                    firstName = data["firstName"] as? String ?: "",
                    lastName = data["lastName"] as? String ?: "",
                    birthday = data["birthday"] as? String,
                    imageUrl = (data["imageUrl"] as? List<String>) ?: emptyList(),
                    avatarUrl = data["avatarUrl"] as? String,
                    gender = data["gender"] as? String,
                    job = data["job"] as? String,
                    location = data["location"] as? String,
                    description = data["description"] as? String,
                    interests = (data["interests"] as? List<String>) ?: emptyList(),
                    distance = (data["distance"] as? Long)?.toInt()
                )
            }
            allUsers.addAll(users)
        }
        return allUsers
    }

}
