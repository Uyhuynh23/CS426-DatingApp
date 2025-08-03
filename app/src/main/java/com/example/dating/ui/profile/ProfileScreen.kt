// ui/profile/ProfileDetailsScreen.kt
package com.example.dating.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.ui.components.CustomCalendarDialog
import java.text.SimpleDateFormat
import java.util.*
import com.example.dating.ui.theme.AppColors
import com.example.dating.R
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dating.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("David") }
    var lastName by remember { mutableStateOf("Peterson") }
    var birthday by remember { mutableStateOf<Date?>(null) }
    var selectedImageUrl by remember { mutableStateOf<android.net.Uri?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    val profileViewModel: ProfileViewModel = viewModel()
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUrl = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header with Skip button
        Box(modifier = Modifier.fillMaxWidth()) {
            // Back
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.Text_Pink,)
            }

            // Skip
            Text(
                text = "Skip",
                color = AppColors.Text_Pink,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { navController.navigate("gender_select") }
            )
        }



        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Profile details",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Text_LightBlack
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Avatar Section với Icons.Person
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            // Main avatar circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUrl != null) {
                    AsyncImage(
                        model = selectedImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default avatar - sử dụng Icons.Default.Person
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Camera icon for editing
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.Main_Secondary1)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = AppColors.Main_Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // First Name Field
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Text_Pink,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Last Name Field
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Text_Pink,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Birthday Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCalendar = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile_calender),
                    contentDescription = "Calendar",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = birthday?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "Choose birthday date",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Main_Primary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Confirm Button
        Button(
            onClick = {
                isSaving = true
                saveError = null
                profileViewModel.saveProfile(
                    firstName = firstName,
                    lastName = lastName,
                    birthday = birthday?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) },
                    imageUrl = selectedImageUrl?.toString(),
                    onSuccess = {
                        isSaving = false
                        navController.navigate("gender_select")
                    },
                    onFailure = { e ->
                        isSaving = false
                        saveError = e.message
                    }
                )
            },
            enabled = firstName.isNotBlank() && lastName.isNotBlank() && birthday != null && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Main_Secondary1,
                disabledContainerColor = Color.LightGray
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AppColors.Main_Primary)
            } else {
                Text(
                    text = "Confirm",
                    color = AppColors.Main_Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (saveError != null) {
            Text(
                text = saveError ?: "",
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Custom Calendar Dialog
    if (showCalendar) {
        CustomCalendarDialog(
            onDateSelected = { selectedDate ->
                birthday = selectedDate
                showCalendar = false
            },
            onDismiss = { showCalendar = false }
        )
    }
}
