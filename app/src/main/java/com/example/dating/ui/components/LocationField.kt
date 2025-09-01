package com.example.dating.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LocationField(
    location: String,
    isEditMode: Boolean,
    onLocationChange: (String) -> Unit
) {
    OutlinedTextField(
        value = location,
        onValueChange = { if (isEditMode && it.length <= 70) onLocationChange(it) },
        label = { Text("Location") },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEditMode,
        readOnly = !isEditMode,
        singleLine = true
    )
}