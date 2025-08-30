package com.example.dating.ui.mainscreens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import com.example.dating.ui.theme.AppColors
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Calendar
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.util.lerp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.Scaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dating.viewmodel.HomeViewModel
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.ui.components.BottomNavigationBar
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.dating.navigation.Screen

@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = hiltViewModel()) {
    val uidResource by homeViewModel.profilesState.collectAsState()
    val usersResource by homeViewModel.usersState.collectAsState()
    val profileIndex = rememberSaveable { mutableStateOf(0) }

    // Helper functions
    suspend fun handleProfileAction(isLike: Boolean, profileIndex: MutableState<Int>, profiles: List<User>, homeViewModel: HomeViewModel, navController: NavController) {
        val currentProfile = profiles.getOrNull(profileIndex.value)
        if (currentProfile != null) {
            if (isLike) {
                val likedUserId = currentProfile.uid
                if (likedUserId != null) {
                    homeViewModel.likeProfile(likedUserId)
                    // Check for match and navigate if found
                    val matchId = homeViewModel.matchFoundUserId.value
                    if (matchId != null) {
                        navController.navigate("match")
                    }
                }
            }
            profileIndex.value++
        }
    }
    suspend fun animateSwipe(offsetX: Animatable<Float, *>, direction: Float) {
        offsetX.animateTo(direction * 400f, tween(300))
        offsetX.snapTo(0f)
    }

    // Only fetch profiles once when the composable is first composed
    LaunchedEffect(Unit) {
        if (uidResource is Resource.Success && (uidResource as Resource.Success<List<String>>).result.isEmpty()) {
            Log.d("HomeScreen", "fetchHome called, uidResource is empty")
            homeViewModel.fetchHome()
        }
    }

    // Fetch users by IDs when uidResource changes
    LaunchedEffect(uidResource) {
        if (uidResource is Resource.Success) {
            val uids = (uidResource as Resource.Success<List<String>>).result
            Log.d("HomeScreen", "Fetching users by IDs: $uids")
            homeViewModel.getUserProfilesByIds(uids)
        }
    }

    // Observe matchFoundUserId and navigate if a match is found
    val matchFoundUserId by homeViewModel.matchFoundUserId.collectAsState()
    LaunchedEffect(matchFoundUserId) {
        if (matchFoundUserId != null) {
            navController.navigate("match/${matchFoundUserId}")
            homeViewModel.resetMatchFoundUserId()
        }
    }


    // Use Box to overlay BottomNavigationBar and keep it fixed at the bottom
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, 0)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.MainBackground)
        ) {
            HomeHeader(navController)

            when (uidResource) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Failure -> {
                    val error = (uidResource as Resource.Failure).exception?.message ?: "Unknown error"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: $error", color = Color.Red)
                    }
                }

                is Resource.Success -> {
                    when (usersResource) {
                        is Resource.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is Resource.Failure -> {
                            val error = (usersResource as Resource.Failure).exception?.message ?: "Unknown error"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Error: $error", color = Color.Red)
                            }
                        }

                        is Resource.Success -> {
                            val profiles = (usersResource as Resource.Success<List<User>>).result

                            // Chiếm phần còn lại của màn hình
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileCard(
                                    profiles = profiles,
                                    profileIndex = profileIndex,
                                    handleProfileAction = { isLike, profileIndex, profiles, homeViewModel ->
                                        handleProfileAction(
                                            isLike,
                                            profileIndex,
                                            profiles,
                                            homeViewModel,
                                            navController
                                        )
                                    },
                                    animateSwipe = ::animateSwipe,
                                    navController = navController // Pass navController here
                                )
                            }

                            // Đặt dưới cùng, không bị che
                            ActionButtons(
                                profiles = profiles,
                                profileIndex = profileIndex,
                                handleProfileAction = { isLike, profileIndex, profiles, homeViewModel ->
                                    handleProfileAction(
                                        isLike,
                                        profileIndex,
                                        profiles,
                                        homeViewModel,
                                        navController
                                    )
                                },
                                animateSwipe = ::animateSwipe
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun HomeHeader(navController: NavController) {
    val showFilterDialog = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigate("profile_details") }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = AppColors.Text_Pink
            )
        }

        Text(
            text = "Discover",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        IconButton(onClick = { showFilterDialog.value = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Filter",
                tint = AppColors.Text_Pink
            )
        }
    }
    if (showFilterDialog.value) {
        com.example.dating.ui.components.FilterDialog(
            show = showFilterDialog.value,
            onDismiss = { showFilterDialog.value = false },
            onApply = { selectedInterest, location, distance, ageRange ->
                // TODO: Apply filter logic here
                showFilterDialog.value = false
            }
        )
    }
}

@Composable
fun ProfileCard(
    profiles: List<User>,
    profileIndex: MutableState<Int>,
    handleProfileAction: suspend (Boolean, MutableState<Int>, List<User>, HomeViewModel) -> Unit,
    animateSwipe: suspend (Animatable<Float, *>, Float) -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    navController: NavController
) {
    val currentProfile = profiles.getOrNull(profileIndex.value)
    if (currentProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(900.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No profiles available", color = Color.Gray)
        }
        return
    }

    val firstName = currentProfile.firstName ?: ""
    val lastName = currentProfile.lastName ?: ""
    val name = (firstName + " " + lastName).trim().ifEmpty { "Unknown" }
    val birthday = currentProfile.birthday
    Log.d("YearBug", "Year: $birthday")

    val age = birthday?.let {
        try {
            // Expecting format dd/MM/yyyy
            val year = it.split("/").getOrNull(2)?.toInt() ?: throw Exception("Invalid date")
            Log.d("YearBug", "Year: $year")
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            (currentYear - year).toString()
        } catch (e: Exception) {
            "?"
        }
    } ?: "?"
    val description = currentProfile.description ?: "No description"
    val distance = currentProfile.distance?.toString() ?: "1 km"

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val isDragging = remember { mutableStateOf(false) }
    val likeProgress = (offsetX.value / 200f).coerceIn(0f, 1f)
    val dislikeProgress = (-offsetX.value / 200f).coerceIn(0f, 1f)
    val iconAlpha = maxOf(likeProgress, dislikeProgress)
    val iconScale = 1f + 0.3f * iconAlpha
    val cardRotation = (offsetX.value / 15).coerceIn(-25f, 25f)
    val threshold = 200f
    val nextProfile = profiles.getOrNull(profileIndex.value + 1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        // Next card (subtle scale/alpha)
        if (nextProfile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF23222B))
                    .graphicsLayer(
                        scaleX = lerp(0.95f, 1f, iconAlpha),
                        scaleY = lerp(0.95f, 1f, iconAlpha),
                        alpha = lerp(0.7f, 1f, iconAlpha)
                    )
            ) {}
        }
        // Top card (draggable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                .rotate(cardRotation)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF23222B))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            currentProfile.uid?.let { uid ->
                                Log.d("Navigation", "Double tap detected, navigating to user profile with uid: $uid")
                                navController.navigate(Screen.UserProfile.route(uid))
                            }
                        }
                    )
                }
                .pointerInput(profileIndex.value) {
                    detectDragGestures(
                        onDragStart = { isDragging.value = true },
                        onDragEnd = {
                            isDragging.value = false
                            scope.launch {
                                when {
                                    offsetX.value > threshold -> {
                                        handleProfileAction(true, profileIndex, profiles, homeViewModel)
                                        animateSwipe(offsetX, 1f)
                                        offsetY.snapTo(0f)
                                    }
                                    offsetX.value < -threshold -> {
                                        handleProfileAction(false, profileIndex, profiles, homeViewModel)
                                        animateSwipe(offsetX, -1f)
                                        offsetY.snapTo(0f)
                                    }
                                    else -> {
                                        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                        offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Portrait Image (placeholder)
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
            )
            // Distance Label
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(distance, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = 300f
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            )
            // Name, Age, Description
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$name, $age",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    color = Color(0xFFCCCCCC),
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            // Like/Dislike Icon Overlay
            if (likeProgress > 0.05f) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.Red.copy(alpha = iconAlpha),
                    modifier = Modifier
                        .size((96f * iconScale).dp)
                        .align(Alignment.Center)
                )
            } else if (dislikeProgress > 0.05f) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dislike",
                    tint = Color.White.copy(alpha = iconAlpha),
                    modifier = Modifier
                        .size((96f * iconScale).dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    profiles: List<User>,
    profileIndex: MutableState<Int>,
    handleProfileAction: suspend (Boolean, MutableState<Int>, List<User>, HomeViewModel) -> Unit,
    animateSwipe: suspend (Animatable<Float, *>, Float) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dislike Button
        ActionButton(
            icon = Icons.Default.Close,
            background = Color.White,
            iconTint = Color.Black,
            size = 75.dp,
            shadow = 8.dp
        ) {
            scope.launch {
                handleProfileAction(false, profileIndex, profiles, homeViewModel)
                animateSwipe(offsetX, -1f) // Pass direction explicitly
            }
        }
        // Super Like Button
        ActionButton(
            icon = Icons.Default.Favorite,
            background = AppColors.Text_Pink,
            iconTint = Color.White,
            size = 95.dp,
            shadow = 12.dp
        ) {
            scope.launch {
                handleProfileAction(true, profileIndex, profiles, homeViewModel)
                animateSwipe(offsetX, 1f) // Pass direction explicitly
            }
        }
        // Like Button (calls a different method for clarity)
        ActionButton(
            icon = Icons.Default.Star,
            background = Color(0xFF4A154B),
            iconTint = Color.White,
            size = 75.dp,
            shadow = 8.dp
        ) {
            scope.launch {
                handleProfileAction(true, profileIndex, profiles, homeViewModel)
                animateSwipe(offsetX, 1f) // Pass direction explicitly
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    background: Color,
    iconTint: Color,
    size: Dp,
    shadow: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(shadow, CircleShape)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
