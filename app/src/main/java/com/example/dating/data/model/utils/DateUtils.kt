package com.example.dating.data.model.utils

import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtils {
    /**
     * Calculates age from a birthday string supporting multiple formats.
     * Supported formats: "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy"
     * Returns null if parsing fails.
     */
    fun calculateAgeFromBirthday(birthday: String?): Int? {
        if (birthday == null) return null
        try {
            val year = birthday.split("/").getOrNull(2)?.toInt()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year != null) return currentYear - year
        } catch (_: Exception) {}
        val formats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy")
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt)
                val date = sdf.parse(birthday)
                if (date != null) {
                    val dobCal = Calendar.getInstance()
                    dobCal.time = date
                    val birthYear = dobCal.get(Calendar.YEAR)
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    return currentYear - birthYear
                }
            } catch (_: Exception) {}
        }
        return null
    }
}