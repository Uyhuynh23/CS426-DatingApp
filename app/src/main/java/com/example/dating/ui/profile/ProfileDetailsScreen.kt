package com.example.dating.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dating.R
import com.example.dating.ui.theme.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.clip
import com.example.dating.ui.components.CustomCalendarDialog
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.example.dating.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileDetailsScreen(
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    val profileViewModel: ProfileViewModel = viewModel()

    // All possible interests (same as InterestSelectionScreen)
    val allInterests = listOf(
        Interest("Photography", R.drawable.ic_interest_photography),
        Interest("Shopping", R.drawable.ic_interest_shopping),
        Interest("Karaoke", R.drawable.ic_interest_karaoke),
        Interest("Yoga", R.drawable.ic_interest_yoga),
        Interest("Cooking", R.drawable.ic_interest_cooking),
        Interest("Tennis", R.drawable.ic_interest_tennis),
        Interest("Run", R.drawable.ic_interest_run),
        Interest("Swimming", R.drawable.ic_interest_swimming),
        Interest("Art", R.drawable.ic_interest_art),
        Interest("Traveling", R.drawable.ic_interest_travelling),
        Interest("Extreme", R.drawable.ic_interest_extreme),
        Interest("Music", R.drawable.ic_interest_music),
        Interest("Drink", R.drawable.ic_interest_drink),
        Interest("Video games", R.drawable.ic_interest_game)
    )

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            errorMessage = "User not logged in."
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            if (doc.exists()) {
                profile = doc.data
            } else {
                errorMessage = "Profile not found."
            }
        } catch (e: Exception) {
            errorMessage = e.message
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            profile != null -> {
                var editableFirstName by remember { mutableStateOf(profile!!["firstName"]?.toString() ?: "") }
                var editableLastName by remember { mutableStateOf(profile!!["lastName"]?.toString() ?: "") }
                var editableBirthday by remember { mutableStateOf(profile!!["birthday"]?.toString() ?: "") }
                var editableGender by remember { mutableStateOf(profile!!["gender"]?.toString() ?: "") }
                var selectedInterests = remember { ((profile!!["interests"] as? List<*>)?.map { it.toString() } ?: emptyList()).toMutableStateList() }
                var isSaving by remember { mutableStateOf(false) }
                var saveError by remember { mutableStateOf<String?>(null) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            if (isEditMode) {
                                isSaving = true
                                saveError = null
                                profileViewModel.saveProfile(
                                    editableFirstName,
                                    editableLastName,
                                    editableBirthday,
                                    profile?.get("imageUrl") as? String,
                                    onSuccess = {
                                        profileViewModel.updateGender(
                                            editableGender,
                                            onSuccess = {
                                                profileViewModel.updateInterests(
                                                    selectedInterests,
                                                    onSuccess = {
                                                        isSaving = false
                                                        isEditMode = false
                                                    },
                                                    onFailure = { e ->
                                                        isSaving = false
                                                        saveError = e.message
                                                    }
                                                )
                                            },
                                            onFailure = { e ->
                                                isSaving = false
                                                saveError = e.message
                                            }
                                        )
                                    },
                                    onFailure = { e ->
                                        isSaving = false
                                        saveError = e.message
                                    }
                                )
                            } else {
                                isEditMode = true
                            }
                        }) {
                            Text(
                                text = if (isEditMode) "Done" else "Edit Profile",
                                color = AppColors.Text_Pink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    if (saveError != null) {
                        Text(
                            text = saveError ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    val imageUrl = profile!!["imageUrl"] as? String
                    if (!imageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                error = painterResource(R.drawable.ic_avatar),
                                placeholder = painterResource(R.drawable.ic_avatar)
                            ),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(top = 16.dp, bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_avatar),
                            contentDescription = "Default Avatar",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editableFirstName,
                            onValueChange = { if (isEditMode) editableFirstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp, bottom = 8.dp),
                            enabled = isEditMode,
                            readOnly = !isEditMode
                        )
                        OutlinedTextField(
                            value = editableLastName,
                            onValueChange = { if (isEditMode) editableLastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp, bottom = 8.dp),
                            enabled = isEditMode,
                            readOnly = !isEditMode
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp, bottom = 8.dp)
                                .clickable(enabled = isEditMode) { if (isEditMode) showCalendar = true }
                        ) {
                            OutlinedTextField(
                                value = editableBirthday,
                                onValueChange = {},
                                label = { Text("Birthday") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditMode, // Always enabled for proper color
                                readOnly = true
                            )
                        }
                        if (isEditMode) {
                            var expanded by remember { mutableStateOf(false) }
                            val genderOptions = listOf("Man", "Woman", "Other")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, bottom = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editableGender,
                                    onValueChange = {},
                                    label = { Text("Gender") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (isEditMode) Modifier.clickable { expanded = true } else Modifier),
                                    enabled = isEditMode, // Always enabled for proper color
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { if (isEditMode) expanded = true }) {
                                            Icon(
                                                painter = painterResource(id = com.example.dating.R.drawable.ic_arrow_drop_down),
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
                                                editableGender = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = editableGender,
                                onValueChange = {},
                                label = { Text("Gender") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, bottom = 8.dp),
                                enabled = false,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.LightGray,
                                    disabledLabelColor = Color.LightGray,
                                    disabledBorderColor = Color.LightGray
                                )
                            )
                        }
                    }
                    if (allInterests.isNotEmpty()) {
                        Text(
                            text = "Interests:",
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f), // Take available vertical space
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(allInterests) { interest ->
                                val isSelected = selectedInterests.contains(interest.name)
                                InterestItem(
                                    interest = interest,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isEditMode) {
                                            if (isSelected) selectedInterests.remove(interest.name)
                                            else selectedInterests.add(interest.name)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (showCalendar) {
                    CustomCalendarDialog(
                        onDateSelected = { selectedDate ->
                            editableBirthday = selectedDate.toString() // or format as needed
                            showCalendar = false
                        },
                        onDismiss = { showCalendar = false }
                    )
                }
            }
        }
    }
}
