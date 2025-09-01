package com.example.dating.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DescriptionField(
    description: String,
    isEditMode: Boolean,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = { if (isEditMode && it.length <= 150) onDescriptionChange(it) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEditMode,
        readOnly = !isEditMode,
        supportingText = { Text("Max 150 characters") },
        maxLines = 3
    )
}