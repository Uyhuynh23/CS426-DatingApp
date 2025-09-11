package com.example.dating.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun JobDropdown(
    job: String,
    isEditMode: Boolean,
    onJobChange: (String) -> Unit
) {
    val jobOptions = listOf(
        "Software Engineer", "Doctor", "Teacher", "Nurse", "Accountant", "Designer",
        "Manager", "Salesperson", "Lawyer", "Pharmacist", "Architect", "Chef",
        "Police Officer", "Firefighter", "Scientist", "Dentist", "Mechanic",
        "Electrician", "Plumber", "Pilot", "Flight Attendant", "Journalist",
        "Photographer", "Artist", "Musician", "Actor", "Writer", "Engineer",
        "Consultant", "Entrepreneur"
    )

    var text by remember { mutableStateOf(job) }
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onJobChange(it) // let user type freely
                expanded = isEditMode && it.isNotBlank() && hasFocus
            },
            label = { Text("Job") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                    expanded = hasFocus && isEditMode && text.isNotBlank()
                },
            enabled = isEditMode,
            singleLine = true,
            trailingIcon = {
                if (isEditMode) {
                    IconButton(onClick = { expanded = !expanded && hasFocus }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )

        if (expanded) {
            val filtered = jobOptions.filter {
                it.contains(text, ignoreCase = true) && it != text
            }
            if (filtered.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 4.dp)
                ) {
                    items(filtered) { option ->
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable {
                                    text = option
                                    onJobChange(option)
                                    expanded = false
                                }
                        )
                    }
                }
            }
        }
    }
}
