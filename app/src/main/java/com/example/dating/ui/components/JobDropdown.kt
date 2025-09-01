package com.example.dating.ui.components
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import com.example.dating.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDropdown(
    job: String,
    isEditMode: Boolean,
    onJobChange: (String) -> Unit
) {
    val jobOptions = listOf(
        "Software Engineer", "Doctor", "Teacher", "Nurse", "Accountant", "Designer", "Manager", "Salesperson", "Lawyer", "Pharmacist",
        "Architect", "Chef", "Police Officer", "Firefighter", "Scientist", "Dentist", "Mechanic", "Electrician", "Plumber", "Pilot",
        "Flight Attendant", "Journalist", "Photographer", "Artist", "Musician", "Actor", "Writer", "Engineer", "Consultant", "Entrepreneur"
    )
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = job,
            onValueChange = {},
            label = { Text("Job") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isEditMode) { if (isEditMode) showDialog = true },
            enabled = isEditMode,
            readOnly = !isEditMode,
            trailingIcon = {
                IconButton(onClick = { if (isEditMode) showDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                        contentDescription = "Dropdown"
                    )
                }
            }
        )
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {},
                title = { Text("Select Job") },
                text = {
                    Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                        jobOptions.forEach { option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onJobChange(option)
                                        showDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                color = if (option == job) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    }
}