package com.example.dating.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.ProfileViewModel
import com.example.dating.data.model.UserFilterPreferences


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    userViewModel: ProfileViewModel,
    currentUid: String,
    onApply: (selectedInterest: String, location: String, distance: Float, ageRange: ClosedFloatingPointRange<Float>) -> Unit
) {
    if (!show) return
    val userState by userViewModel.user.collectAsState()
    val filterPrefs = userState?.filterPreferences
    var selectedInterest by remember { mutableStateOf(filterPrefs?.preferredGender ?: "Female") }
    var location by remember { mutableStateOf(userState?.location ?: "Chicago, USA") }
    var distance by remember { mutableStateOf(filterPrefs?.maxDistance?.toFloat() ?: 40f) }
    var ageRange by remember { mutableStateOf((filterPrefs?.minAge?.toFloat() ?: 20f)..(filterPrefs?.maxAge?.toFloat() ?: 28f)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val prefs = UserFilterPreferences(
                        preferredGender = selectedInterest,
                        minAge = ageRange.start.toInt(),
                        maxAge = ageRange.endInclusive.toInt(),
                        maxDistance = distance.toInt()
                    )
                    userViewModel.updateFilterPreferences(currentUid, prefs) { /* handle result if needed */ }
                    onApply(selectedInterest, location, distance, ageRange)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Main_Secondary1),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Continue", color = AppColors.Main_Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                TextButton(onClick = {
                    selectedInterest = "Female"
                    location = "Chicago, USA"
                    distance = 40f
                    ageRange = 20f..28f
                }) {
                    Text("Clear", color = Color(0xFFFF69B4), fontSize = 16.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Interested in
                Text("Interested in", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterSegmentButton("Female", selectedInterest == "Female", onClick = { selectedInterest = "Female" }, modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
                    FilterSegmentButton("Male", selectedInterest == "Male", onClick = { selectedInterest = "Male" }, modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
                    FilterSegmentButton("Both", selectedInterest == "Both", onClick = { selectedInterest = "Both" }, modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Location
                Text("Location", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    readOnly = false,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Select location", tint = AppColors.Text_Pink)
                    },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Distance
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Distance", fontSize = 16.sp, color = Color.Gray)
                    Text("${distance.toInt()}km", fontSize = 16.sp, color = Color.Gray)
                }
                Slider(
                    value = distance,
                    onValueChange = { distance = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Text_Pink,
                        activeTrackColor = AppColors.Text_Pink,
                        inactiveTrackColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Age
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Age", fontSize = 16.sp, color = Color.Gray)
                    Text("${ageRange.start.toInt()}-${ageRange.endInclusive.toInt()}", fontSize = 16.sp, color = Color.Gray)
                }
                RangeSlider(
                    value = ageRange,
                    onValueChange = { ageRange = it },
                    valueRange = 18f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Text_Pink,
                        activeTrackColor = AppColors.Text_Pink,
                        inactiveTrackColor = Color.LightGray
                    )
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun FilterSegmentButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AppColors.Main_Secondary1 else Color.LightGray,
            contentColor = if (selected) AppColors.Main_Primary else Color.Black
        ),
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, maxLines = 1)
    }
}
