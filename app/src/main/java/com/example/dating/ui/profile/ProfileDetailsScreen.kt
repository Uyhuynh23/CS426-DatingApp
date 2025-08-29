package com.example.dating.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dating.R
import com.example.dating.ui.components.CustomCalendarDialog
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.ProfileViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import com.example.dating.data.model.Interest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val user by profileViewModel.user.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (updateState) {
            is Resource.Loading -> {
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                }
            }
            is Resource.Failure -> {
                val exception = (updateState as Resource.Failure).exception
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                ) {
                    Text(
                        text = exception.localizedMessage ?: "Update failed.",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            else -> {
                user?.let {
                    ProfileContent(
                        navController = navController,
                        initialProfile = it,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    navController: NavController,
    initialProfile: User, // Change type to User
    profileViewModel: ProfileViewModel
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }

    var editableFirstName by remember { mutableStateOf(initialProfile.firstName) }
    var editableLastName by remember { mutableStateOf(initialProfile.lastName) }
    var editableBirthday by remember { mutableStateOf(initialProfile.birthday ?: "") }
    var editableGender by remember { mutableStateOf(initialProfile.gender ?: "") }
    var editableJob by remember { mutableStateOf(initialProfile.job ?: "") }
    var editableLocation by remember { mutableStateOf(initialProfile.location ?: "") }
    var editableDescription by remember { mutableStateOf(initialProfile.description ?: "") }

    val selectedInterests = remember {
        initialProfile.interests.toMutableStateList()
    }

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    val allInterests = remember {
        listOf(
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
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Back Button and Edit/Done Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.Text_Pink)
                }
                TextButton(onClick = {
                    if (isEditMode) {
                        isSaving = true
                        saveError = null
                        val user = User(
                            uid = initialProfile.uid,
                            firstName = editableFirstName,
                            lastName = editableLastName,
                            birthday = editableBirthday,
                            imageUrl = initialProfile.imageUrl,
                            avatarUrl = initialProfile.avatarUrl,
                            gender = editableGender,
                            job = editableJob,
                            location = editableLocation,
                            description = editableDescription,
                            interests = selectedInterests.toList()
                        )
                        profileViewModel.updateProfile(user)
                        isSaving = false
                        isEditMode = false
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
        }


        // Saving Indicator and Error Message
        item {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            }
            if (saveError != null) {
                Text(
                    text = saveError ?: "Unknown error while saving",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Profile Image
        item {
            val imageUrl = initialProfile.avatarUrl ?: initialProfile.imageUrl.firstOrNull()
            val imageModifier = Modifier.size(170.dp).padding(vertical = 8.dp)
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl, error = painterResource(R.drawable.ic_avatar)),
                    contentDescription = "Profile Image",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_avatar),
                    contentDescription = "Default Avatar",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Form Fields
        item {
            NameFields(firstName = editableFirstName, lastName = editableLastName, isEditMode = isEditMode,
                onFirstNameChange = { editableFirstName = it },
                onLastNameChange = { editableLastName = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BirthdayGenderFields(birthday = editableBirthday, gender = editableGender, isEditMode = isEditMode,
                showCalendar = showCalendar,
                onShowCalendar = { showCalendar = true },
                onGenderChange = { editableGender = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            JobDropdown(job = editableJob, isEditMode = isEditMode, onJobChange = { editableJob = it })
            Spacer(modifier = Modifier.height(8.dp))
            LocationField(location = editableLocation, isEditMode = isEditMode,
                onLocationChange = { editableLocation = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DescriptionField(description = editableDescription, isEditMode = isEditMode,
                onDescriptionChange = { editableDescription = it }
            )
        }

        // Interests Section
        item {
            Text(
                text = "Interests:",
                fontSize = 18.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        item {
            InterestsSection(
                allInterests = allInterests,
                selectedInterests = selectedInterests,
                isEditMode = isEditMode
            )
        }
    }

    if (showCalendar) {
        CustomCalendarDialog(
            onDateSelected = { selectedDate ->
                editableBirthday = selectedDate.toString() // Ensure your date format is correct
                showCalendar = false
            },
            onDismiss = { showCalendar = false }
        )
    }
}

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
            modifier = Modifier.weight(1f).padding(end = 4.dp, bottom = 8.dp)
                .clickable(enabled = isEditMode) { if (isEditMode) onShowCalendar() }
        ) {
            OutlinedTextField(
                value = birthday,
                onValueChange = {},
                label = { Text("Birthday") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode,
                readOnly = true
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
                readOnly = true,
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
    var jobDropdownExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = jobDropdownExpanded,
        onExpandedChange = { if (isEditMode) jobDropdownExpanded = !jobDropdownExpanded }
    ) {
        OutlinedTextField(
            value = job,
            onValueChange = { if (isEditMode) onJobChange(it) },
            label = { Text("Job") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditMode,
            readOnly = false,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobDropdownExpanded)
            }
        )
        ExposedDropdownMenu(
            expanded = jobDropdownExpanded,
            onDismissRequest = { jobDropdownExpanded = false }
        ) {
            jobOptions.filter { it.contains(job, ignoreCase = true) || job.isBlank() }
                .forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onJobChange(option)
                            jobDropdownExpanded = false
                        }
                    )
                }
        }
    }
}

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsSection(
    allInterests: List<Interest>,
    selectedInterests: SnapshotStateList<String>,
    isEditMode: Boolean
) {
    // Display interests in rows of two using chunked
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        allInterests.chunked(2).forEach { rowInterests ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                rowInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest.name)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) AppColors.Text_Pink.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                        border = if (isSelected) BorderStroke(2.dp, AppColors.Text_Pink) else null,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                            .height(48.dp)
                            .clickable(enabled = isEditMode) {
                                if (isEditMode) {
                                    if (isSelected) selectedInterests.remove(interest.name)
                                    else selectedInterests.add(interest.name)
                                }
                            },
                        tonalElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = interest.icon),
                                contentDescription = interest.name,
                                tint = if (isSelected) AppColors.Text_Pink else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = interest.name,
                                color = if (isSelected) AppColors.Text_Pink else Color.DarkGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                // If the row has only one interest, add a Spacer to fill the second column
                if (rowInterests.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}