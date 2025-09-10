package com.example.dating.data.model.repository

import com.example.dating.data.model.UserFilterPreferences
import com.example.dating.data.model.utils.DateUtils
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class FilteringRepository @Inject constructor() {
    fun filterUser(
        doc: DocumentSnapshot,
        currentUserId: String,
        currentUserDoc: DocumentSnapshot,
        filterPrefs: UserFilterPreferences?
    ): Boolean {
        val uid = doc.id
        if (uid == currentUserId) return false
        val gender = doc.getString("gender")
        val birthday = doc.getString("birthday")
        val location = doc.getString("location")
        val distance = (doc.get("distance") as? Long)?.toInt()
        val lat = (doc.get("lat") as? Double)
            ?: (doc.get("lat") as? Float)?.toDouble()
            ?: (doc.get("lat") as? Long)?.toDouble()
        val lng = (doc.get("lng") as? Double)
            ?: (doc.get("lng") as? Float)?.toDouble()
            ?: (doc.get("lng") as? Long)?.toDouble()
        val currentUserLat = (currentUserDoc.get("lat") as? Double)
            ?: (currentUserDoc.get("lat") as? Float)?.toDouble()
            ?: (currentUserDoc.get("lat") as? Long)?.toDouble()
        val currentUserLng = (currentUserDoc.get("lng") as? Double)
            ?: (currentUserDoc.get("lng") as? Float)?.toDouble()
            ?: (currentUserDoc.get("lng") as? Long)?.toDouble()

        // --- NEW FILTER: doc's filterPreferences.preferredGender must match current user's gender ---
        val docFilterPrefs = doc.get("filterPreferences") as? Map<*, *>
        val docPreferredGender = docFilterPrefs?.get("preferredGender") as? String
        val currentUserGender = currentUserDoc.getString("gender")
        if (docPreferredGender != null && docPreferredGender != "Both" && currentUserGender != docPreferredGender) {
            return false
        }

        if (!filterGender(gender, filterPrefs)) return false
        if (!filterAge(birthday, filterPrefs)) return false
        if (!filterDistance(distance, filterPrefs)) return false
        if (!filterLatLng(lat, lng, currentUserLat, currentUserLng, filterPrefs)) return false
        return true
    }

    private fun filterGender(gender: String?, filterPrefs: UserFilterPreferences?): Boolean {
        return !(filterPrefs?.preferredGender != null && filterPrefs.preferredGender != "Both" && gender != filterPrefs.preferredGender)
    }

    private fun filterAge(birthday: String?, filterPrefs: UserFilterPreferences?): Boolean {
        if (filterPrefs?.minAge == null || filterPrefs.maxAge == null || birthday == null) return true
        val age = DateUtils.calculateAgeFromBirthday(birthday)
        return !(age == null || age < filterPrefs.minAge || age > filterPrefs.maxAge)
    }

    private fun filterDistance(distance: Int?, filterPrefs: UserFilterPreferences?): Boolean {
        return !(filterPrefs?.maxDistance != null && distance != null && distance > filterPrefs.maxDistance)
    }

    private fun filterLatLng(lat: Double?, lng: Double?, currentUserLat: Double?, currentUserLng: Double?, filterPrefs: UserFilterPreferences?): Boolean {
        if (filterPrefs?.maxDistance == null || lat == null || lng == null || currentUserLat == null || currentUserLng == null) return true
        val distanceKm = haversine(currentUserLat, currentUserLng, lat, lng)
        return !(distanceKm > filterPrefs.maxDistance)
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
