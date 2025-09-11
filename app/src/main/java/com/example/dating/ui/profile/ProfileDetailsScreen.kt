package com.example.dating.ui.profile

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dating.R
import com.example.dating.data.model.ALL_INTERESTS
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.ui.components.BirthdayGenderFields
import com.example.dating.ui.components.CustomCalendarDialog
import com.example.dating.ui.components.DescriptionField
import com.example.dating.ui.components.InterestsSection
import com.example.dating.ui.components.JobDropdown
import com.example.dating.ui.components.LocationField
import com.example.dating.ui.components.NameFields
import com.example.dating.ui.components.CountryData
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.AuthViewModel
import com.example.dating.viewmodel.ProfileViewModel
import com.example.dating.viewmodel.StoryViewModel
import org.json.JSONArray
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    storyViewModel: StoryViewModel = hiltViewModel(),
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
                        storyViewModel = storyViewModel,
                        authViewModel = authViewModel // <-- Pass AuthViewModel
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
    storyViewModel: StoryViewModel,
    authViewModel: AuthViewModel
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

    // Observe user's own stories
    val myStories by storyViewModel.myStories.collectAsState()
    val validStories = myStories.filter { it.expiresAt ?: 0 > System.currentTimeMillis() }

    val context = LocalContext.current
    var countries by remember { mutableStateOf<List<CountryData>>(emptyList()) }

    LaunchedEffect(Unit) {
        countries = loadCountriesFromAssets(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Back Arrow and Log out button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.Text_Pink
                    )
                }
                TextButton(
                    onClick = {
                        authViewModel.logout(profileViewModel)
                        authViewModel.clearGoogleSignInState()
                        navController.navigate("login") {
                            popUpTo("root_graph") { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Log out",
                        color = AppColors.Text_Pink,
                        fontWeight = FontWeight.Bold
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

        // Show user's own story bubble if any
        item {
            if (validStories.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                        .clickable {
                            navController.navigate("story_viewer/${initialProfile.uid}")
                        },
                    colors = CardDefaults.cardColors(containerColor = AppColors.Main_Secondary1)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Show avatar with pink border
                        Image(
                            painter = rememberAsyncImagePainter(initialProfile.avatarUrl ?: initialProfile.imageUrl.firstOrNull()),
                            contentDescription = "Your Story",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(3.dp, AppColors.Text_Pink, CircleShape)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Your Story",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppColors.Text_Pink
                            )
                            Text(
                                text = "Tap to view",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
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
                                    interests = selectedInterests.toList(),
                                    filterPreferences = initialProfile.filterPreferences
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
                        birthday = editableBirthday,
                        gender = editableGender,
                        isEditMode = isEditMode,
                        showCalendar = showCalendar,
                        onShowCalendar = { showCalendar = !showCalendar }, // <-- Toggle state
                        onBirthdayChange = { editableBirthday = it },
                        onGenderChange = { editableGender = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JobDropdown(
                        job = editableJob,
                        isEditMode = isEditMode,
                        onJobChange = { editableJob = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationField(
                        location = editableLocation,
                        isEditMode = isEditMode,
                        onLocationChange = { editableLocation = it },
                        countries = countries
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
                    Spacer(Modifier.height(16.dp))

                    // ===== Gallery (max 5) =====
                    val liveUser by profileViewModel.user.collectAsState()
                    val galleryImages = liveUser?.imageUrl ?: initialProfile.imageUrl

                    GallerySection(
                        navController = navController,
                        images = galleryImages,
                        isEditMode = isEditMode,
                        onAdd = { uri -> profileViewModel.addGalleryImage(uri) },
                        onRemove = { url -> profileViewModel.removeGalleryImage(url) }
                    )
                }
            }
        }
    }

    if (showCalendar) {
        // Parse editableBirthday to Date if available, else null
        val initialDate = try {
            if (editableBirthday.isNotBlank()) {
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).parse(editableBirthday)
            } else null
        } catch (e: Exception) {
            null
        }
        CustomCalendarDialog(
            onDateSelected = { selectedDate ->
                editableBirthday = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(selectedDate)
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
            date = initialDate // <-- Pass initial date here
        )
    }
}

@Composable
private fun GallerySection(
    navController: NavController,
    images: List<String>,
    isEditMode: Boolean,
    onAdd: (android.net.Uri) -> Unit,
    onRemove: (String) -> Unit
) {
    val remaining = (6 - images.size).coerceAtLeast(0)

    val addImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onAdd(uri)
    }

    Column(Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (isEditMode) {
                TextButton(
                    onClick = { if (remaining > 0) addImageLauncher.launch("image/*") },
                ) {
                    Text(
                        if (remaining > 0) "Add photo ($remaining left)" else "Capacity full",
                        color = AppColors.Text_Pink
                    )
                }
            } else {
                if (images.isNotEmpty()) {
                    TextButton(onClick = {
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.set("images", ArrayList(images))
                        navController.navigate(
                            com.example.dating.navigation.Screen.PhotoViewer.route(0)
                        )
                    }) { Text("See all", color = AppColors.Text_Pink) }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (images.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No images available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                itemsIndexed(images) { index, url ->
                    Box {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            placeholder = painterResource(com.example.dating.R.drawable.ic_avatar),
                            error = painterResource(com.example.dating.R.drawable.ic_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isEditMode) {
                                    navController.currentBackStackEntry?.savedStateHandle
                                        ?.set("images", ArrayList(images))
                                    navController.navigate(
                                        com.example.dating.navigation.Screen.PhotoViewer.route(index)
                                    )
                                }
                        )

                        // Delete Button (exist in editing mode)
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .align(Alignment.TopEnd)
                                    .clickable { onRemove(url) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("×", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to load countries from assets
fun loadCountriesFromAssets(context: Context): List<CountryData> {
    return try {
        val inputStream = context.assets.open("countries.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            val name = obj.getString("name")
            val citiesJson = obj.getJSONArray("cities")
            val cities = List(citiesJson.length()) { j -> citiesJson.getString(j) }
            CountryData(name, cities)
        }
    } catch (e: Exception) {
        emptyList()
    }
}
