package com.example.dating.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.dating.ui.components.InterestsSection
import com.example.dating.ui.components.JobDropdown
import com.example.dating.ui.components.LocationField
import com.example.dating.ui.components.NameFields
import com.example.dating.ui.components.DescriptionField
import com.example.dating.ui.components.BirthdayGenderFields
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.ProfileViewModel
import com.example.dating.viewmodel.StoryViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import com.example.dating.data.model.Interest
import com.example.dating.data.model.ALL_INTERESTS
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.focus.onFocusChanged
import android.net.Uri
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel() // <-- Add StoryViewModel
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
                        profileViewModel = profileViewModel,
                        storyViewModel = storyViewModel // <-- Pass StoryViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    navController: NavController,
    initialProfile: User,
    profileViewModel: ProfileViewModel,
    storyViewModel: StoryViewModel
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    val postState by storyViewModel.postState.collectAsState()

    var editableFirstName by remember { mutableStateOf(initialProfile.firstName) }
    var editableLastName by remember { mutableStateOf(initialProfile.lastName) }
    var editableBirthday by remember { mutableStateOf(initialProfile.birthday ?: "") }
    var editableGender by remember { mutableStateOf(initialProfile.gender ?: "") }
    var editableJob by remember { mutableStateOf(initialProfile.job ?: "") }
    var editableLocation by remember { mutableStateOf(initialProfile.location ?: "") }
    var editableDescription by remember { mutableStateOf(initialProfile.description ?: "") }

    // Image picker launcher for avatar
    var selectedImageUrl by remember { mutableStateOf<android.net.Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUrl = uri
        if (uri != null) {
            profileViewModel.uploadAvatar(uri)
        }
    }

    val selectedInterests = remember {
        initialProfile.interests.toMutableStateList()
    }

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    val allInterests = remember { ALL_INTERESTS }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Back Arrow only
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.Text_Pink
                    )
                }
            }
        }

        // Avatar centered with overlapping camera icon
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(140.dp)
                ) {
                    val imageUrl = initialProfile.avatarUrl ?: initialProfile.imageUrl.firstOrNull()
                    val imageModifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, AppColors.Text_Pink, CircleShape)
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
                    // Camera icon overlaps avatar (bottom-end)
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .clip(CircleShape)
                            .background(AppColors.Main_Secondary1)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Avatar",
                            tint = AppColors.Main_Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Add Story Button
        item {
            Button(
                onClick = {
                    navController.navigate("post_story")
                },
                modifier = Modifier
                    .width(180.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Text_Pink)
            ) {
                Text("Add Story", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Card: Edit button, user info, interests
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEditMode) AppColors.Main_Secondary1 else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Edit/Done Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                text = if (isEditMode) "Done" else "Edit",
                                color = AppColors.Text_Pink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Saving Indicator and Error Message
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

                    // User Info Fields
                    NameFields(
                        firstName = editableFirstName, lastName = editableLastName, isEditMode = isEditMode,
                        onFirstNameChange = { editableFirstName = it },
                        onLastNameChange = { editableLastName = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BirthdayGenderFields(
                        birthday = editableBirthday, gender = editableGender, isEditMode = isEditMode,
                        showCalendar = showCalendar,
                        onShowCalendar = { showCalendar = true },
                        onGenderChange = { editableGender = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JobDropdown(
                        job = editableJob,
                        isEditMode = isEditMode,
                        onJobChange = { editableJob = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationField(
                        location = editableLocation, isEditMode = isEditMode,
                        onLocationChange = { editableLocation = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DescriptionField(
                        description = editableDescription, isEditMode = isEditMode,
                        onDescriptionChange = { editableDescription = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Interests Section inside card
                    Text(
                        text = "Interests",
                        fontSize = 20.sp,
                        color = AppColors.Text_Pink,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    InterestsSection(
                        allInterests = allInterests,
                        selectedInterests = selectedInterests,
                        isEditMode = isEditMode
                    )
                }
            }
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
