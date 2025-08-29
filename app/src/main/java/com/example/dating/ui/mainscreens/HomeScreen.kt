package com.example.dating.ui.mainscreens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.R
import com.example.dating.navigation.Screen
import com.example.dating.ui.theme.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val profiles = remember { mutableStateListOf<Map<String, Any>>() }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("users").get().await()
                val allProfiles = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    // gắn doc.id vào "uid" để điều hướng
                    if (doc.id != currentUserId && data != null) data + ("uid" to doc.id) else null
                }
                Log.d("ProfileDebug", "Loaded profiles: $allProfiles")
                profiles.clear()
                profiles.addAll(allProfiles)
                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = e.message
                isLoading.value = false
            }
        }
    }

    val bg = remember {
        Brush.verticalGradient(listOf(Color(0xFFFCE7F3), Color.White))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val profileIndex = remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            HomeHeader(navController)
            when {
                isLoading.value -> Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage.value != null -> Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${errorMessage.value}", color = Color.Red)
                }
                else -> {
                    ProfileCard(
                        navController = navController,
                        profiles = profiles,
                        profileIndex = profileIndex
                    )
                    ActionButtons(
                        profiles = profiles,
                        profileIndex = profileIndex,
                        onLike = {},
                        onDislike = {}
                    )
                }
            }
        }

        // Bottom bar cố định
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun HomeHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.Text_Pink
            )
        }

        Text(
            text = "Discover",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        IconButton(onClick = { /* mở profile */ }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = AppColors.Text_Pink
            )
        }
    }
}

@Composable
fun ProfileCard(
    navController: NavController,
    profiles: List<Map<String, Any>>,
    profileIndex: MutableState<Int>,
    onLike: (Map<String, Any>) -> Unit = {},
    onDislike: (Map<String, Any>) -> Unit = {}
) {
    val currentProfile = profiles.getOrNull(profileIndex.value)

    if (currentProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(900.dp),
            contentAlignment = Alignment.Center
        ) { Text("No profiles available", color = Color.Gray) }
        return
    }

    val firstName = currentProfile["firstName"] as? String ?: ""
    val lastName = currentProfile["lastName"] as? String ?: ""
    val name = (firstName + " " + lastName).trim().ifEmpty { "Unknown" }
    val birthday = currentProfile["birthday"] as? String
    val age = birthday?.let {
        try {
            val year = it.split("/").getOrNull(2)?.toInt() ?: throw Exception("Invalid date")
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            (currentYear - year).toString()
        } catch (_: Exception) { "?" }
    } ?: "?"
    val description = currentProfile["description"] as? String ?: "No description"
    val distance = currentProfile["distance"]?.toString() ?: "1 km"

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
        // Card phía sau (nhỏ mờ)
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
            )
        }

        // Card trên cùng (draggable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                .rotate(cardRotation)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF23222B))
                .pointerInput(profileIndex.value) {
                    detectDragGestures(
                        onDragStart = { isDragging.value = true },
                        onDragEnd = {
                            isDragging.value = false
                            scope.launch {
                                when {
                                    offsetX.value > threshold -> {
                                        onLike(currentProfile)
                                        offsetX.animateTo(1000f, tween(350))
                                        offsetX.snapTo(0f); offsetY.snapTo(0f)
                                        profileIndex.value++
                                    }
                                    offsetX.value < -threshold -> {
                                        onDislike(currentProfile)
                                        offsetX.animateTo(-1000f, tween(350))
                                        offsetX.snapTo(0f); offsetY.snapTo(0f)
                                        profileIndex.value++
                                    }
                                    else -> {
                                        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                        offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Ảnh: nếu có avatarUrl thì dùng AsyncImage, còn không để placeholder
            val avatar = (currentProfile["avatar"] as? String).orEmpty()
            if (avatar.isNotBlank()) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Profile Image",
                    placeholder = painterResource(R.drawable.ic_avatar),
                    error = painterResource(R.drawable.ic_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_avatar),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))
                )
            }

            // Distance
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) { Text(distance, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp) }

            // Gradient dưới
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f, endY = 300f
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            )

            // Tên/tuổi + mô tả
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
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

            // Like / Dislike overlay khi kéo
            val likeProgress = (offsetX.value / 200f).coerceIn(0f, 1f)
            val dislikeProgress = (-offsetX.value / 200f).coerceIn(0f, 1f)
            val iconAlpha = maxOf(likeProgress, dislikeProgress)
            val iconScale = 1f + 0.3f * iconAlpha
            if (likeProgress > 0.05f) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.Red.copy(alpha = iconAlpha),
                    modifier = Modifier.size((96f * iconScale).dp).align(Alignment.Center)
                )
            } else if (dislikeProgress > 0.05f) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dislike",
                    tint = Color.White.copy(alpha = iconAlpha),
                    modifier = Modifier.size((96f * iconScale).dp).align(Alignment.Center)
                )
            }

            // 🔥 OVERLAY CLICKABLE: luôn nhận tap để mở UserProfile
            Box(
                Modifier
                    .matchParentSize()
                    .zIndex(2f)
                    .clickable {
                        val uid = (currentProfile["uid"] as? String).orEmpty()
                        Log.d("NAV_DEBUG", "Card tapped, uid=$uid")
                        if (uid.isNotBlank()) {
                            navController.navigate(Screen.UserProfileById.route(uid))
                        }
                    }
            )
        }
    }
}

@Composable
fun ActionButtons(
    profiles: List<Map<String, Any>>,
    profileIndex: MutableState<Int>,
    onLike: (Map<String, Any>) -> Unit = {},
    onDislike: (Map<String, Any>) -> Unit = {}
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
        ActionButton(
            icon = Icons.Default.Close, background = Color.White, iconTint = Color.Black,
            size = 75.dp, shadow = 8.dp
        ) {
            profiles.getOrNull(profileIndex.value)?.let {
                onDislike(it)
                scope.launch {
                    offsetX.animateTo(-400f, tween(300))
                    offsetX.snapTo(0f)
                    profileIndex.value++
                }
            }
        }
        ActionButton(
            icon = Icons.Default.Favorite, background = AppColors.Text_Pink, iconTint = Color.White,
            size = 95.dp, shadow = 12.dp
        ) {
            profiles.getOrNull(profileIndex.value)?.let {
                onLike(it)
                scope.launch {
                    offsetX.animateTo(400f, tween(300))
                    offsetX.snapTo(0f)
                    profileIndex.value++
                }
            }
        }
        ActionButton(
            icon = Icons.Default.Star, background = Color(0xFF4A154B), iconTint = Color.White,
            size = 75.dp, shadow = 8.dp
        ) { /* Super like */ }
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
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun BottomNavIcon(icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) AppColors.Text_Pink else Color(0xFFBDBDBD),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(103.dp)
            .background(Color.White.copy(alpha = 1.0f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavIcon(icon = Icons.Default.ViewModule, isActive = true) { }
            BottomNavIcon(icon = Icons.Default.Favorite, isActive = false) { }
            BottomNavIcon(icon = Icons.Default.Chat, isActive = false) { }
            BottomNavIcon(icon = Icons.Default.Person, isActive = false) { }
        }
    }
}
