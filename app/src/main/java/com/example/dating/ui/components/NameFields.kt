package com.example.dating.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NameFields(
    firstName: String,
    lastName: String,
    isEditMode: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { if (isEditMode) onFirstNameChange(it) },
            label = { Text("First Name") },
            modifier = Modifier.weight(1f).padding(end = 4.dp, bottom = 8.dp),
            enabled = isEditMode,
            readOnly = !isEditMode
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { if (isEditMode) onLastNameChange(it) },
            label = { Text("Last Name") },
            modifier = Modifier.weight(1f).padding(start = 4.dp, bottom = 8.dp),
            enabled = isEditMode,
            readOnly = !isEditMode
        )
    }
}