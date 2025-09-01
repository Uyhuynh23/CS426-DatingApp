package com.example.dating.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.dating.R

@Composable
fun BirthdayGenderFields(
    birthday: String,
    gender: String,
    isEditMode: Boolean,
    showCalendar: Boolean,
    onShowCalendar: () -> Unit,
    onGenderChange: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp, bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = birthday,
                onValueChange = {},
                label = { Text("Birthday") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditMode) { if (isEditMode) onShowCalendar() },
                enabled = isEditMode,
                readOnly = !isEditMode
            )
        }
        var expanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Man", "Woman", "Other")
        Box(
            modifier = Modifier.weight(1f)
                .padding(start = 4.dp, bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                label = { Text("Gender") },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isEditMode) Modifier.clickable { expanded = true } else Modifier),
                enabled = isEditMode,
                readOnly = !isEditMode,
                trailingIcon = {
                    IconButton(onClick = { if (isEditMode) expanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_drop_down),
                            contentDescription = "Dropdown"
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onGenderChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}