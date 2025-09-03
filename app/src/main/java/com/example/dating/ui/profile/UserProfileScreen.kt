package com.example.dating.ui.profile

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.R
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.dating.ui.theme.AppColors
import androidx.compose.foundation.BorderStroke
import com.example.dating.data.model.Resource

data class Interest(
    val name: String,
    val icon: Int
)

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userUid: String? = null,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val usersResource by homeViewModel.usersState.collectAsState()
    val profileIndex by homeViewModel.profileIndex.collectAsState()
    val matchFoundUserId by homeViewModel.matchFoundUserId.collectAsState()

    val profiles = usersResource.let { res ->
        when (res) {
            is Resource.Success -> res.result
            else -> emptyList()
        }
    }
    val user = profiles.getOrNull(profileIndex)
    val isLoading = usersResource is Resource.Loading

    LaunchedEffect(matchFoundUserId) {
        matchFoundUserId?.let { id ->
            navController.navigate("match/$id")
            homeViewModel.resetMatchFoundUserId()
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        user != null -> UserProfileContent(
            navController = navController,
            profiles = profiles,
            profileIndex = profileIndex,
            homeViewModel = homeViewModel,
            onSwipeDone = {
            },
            onSeeAll = { images ->
                navController.currentBackStackEntry?.savedStateHandle?.set("images", ArrayList(images))
                navController.navigate(Screen.PhotoViewer.route(0))
            },
            onImageClick = { index, images ->
                navController.currentBackStackEntry?.savedStateHandle?.set("images", ArrayList(images))
                navController.navigate(Screen.PhotoViewer.route(index))
            }
        )
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            navController.navigateUp()
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileContent(
    navController: NavController,
    profiles: List<User>,
    profileIndex: Int,
    homeViewModel: HomeViewModel,
    onSwipeDone: () -> Unit,
    onSeeAll: (List<String>) -> Unit,
    onImageClick: (Int, List<String>) -> Unit
) {
    val currentProfile = profiles.getOrNull(profileIndex)
    val nextProfile = profiles.getOrNull(profileIndex + 1)
    val images = currentProfile?.imageUrl ?: emptyList()
    val nextImages = nextProfile?.imageUrl ?: emptyList()

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val threshold = 200f
    val rotation = (offsetX.value / 15).coerceIn(-10f, 10f)

    fun handleProfileAction(isLike: Boolean) {
        if (isLike) {
            currentProfile?.uid?.let { uid ->
                homeViewModel.likeProfile(uid)
            }
        }
        homeViewModel.nextProfile()
    }

    suspend fun animateSwipe(direction: Float) {
        offsetX.animateTo(direction * 400f, tween(300))
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(AppColors.MainBackground),
        contentAlignment = Alignment.Center
    ) {
        // Next profile luôn nằm dưới
        if (nextProfile != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = 0.95f,
                            scaleY = 0.95f,
                            alpha = 0.7f,
                            translationY = 32f
                        )
                        .background(AppColors.MainBackground),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 0.dp)
                ) {
                    item {
                        ProfileHero(
                            profile = nextProfile,
                            navController = navController
                        )
                    }
                    item {
                        ProfileActionsRow(
                            onLike = {},
                            onDislike = {},
                            onSuperLike = {}
                        )
                    }
                    item {
                        ProfileInfoCard(
                            user = nextProfile,
                            images = nextImages,
                            onSeeAll = onSeeAll,
                            onImageClick = onImageClick
                        )
                    }
                }
            }
        }

        // Current profile luôn nằm trên + draggable
        if (currentProfile != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                    .graphicsLayer { rotationZ = rotation }
                    .background(AppColors.MainBackground)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        offsetX.value > threshold -> {
                                            animateSwipe(1f)
                                            handleProfileAction(true)
                                        }
                                        offsetX.value < -threshold -> {
                                            animateSwipe(-1f)
                                            handleProfileAction(false)
                                        }
                                        else -> {
                                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                            offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                        }
                                    }
                                }
                            }
                        ) { _, drag ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + drag.x)
                                offsetY.snapTo(offsetY.value + drag.y)
                            }
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 0.dp)
                ) {
                    item {
                        ProfileHero(
                            profile = currentProfile,
                            navController = navController
                        )
                    }
                    item {
                        ProfileActionsRow(
                            onLike = {
                                scope.launch {
                                    animateSwipe(1f)
                                    handleProfileAction(true)
                                }
                            },
                            onDislike = {
                                scope.launch {
                                    animateSwipe(-1f)
                                    handleProfileAction(false)
                                }
                            },
                            onSuperLike = {
                                scope.launch {
                                    animateSwipe(1f)
                                    handleProfileAction(true)
                                }
                            }
                        )
                    }
                    item {
                        ProfileInfoCard(
                            user = currentProfile,
                            images = images,
                            onSeeAll = onSeeAll,
                            onImageClick = onImageClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ActionButton(
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
            .shadow(shadow, CircleShape, clip = false)
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

private fun computeAge(birthday: String?): Int? {
    if (birthday.isNullOrBlank()) return null
    return try {
        val year = when {
            birthday.contains("/") -> birthday.split("/").getOrNull(2)?.toInt()
            birthday.contains("-") -> birthday.split("-").getOrNull(0)?.toInt()
            else -> null
        } ?: return null
        Calendar.getInstance().get(Calendar.YEAR) - year
    } catch (_: Exception) { null }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileInfoCard(
    user: User,
    images: List<String>,
    onSeeAll: (List<String>) -> Unit,
    onImageClick: (Int, List<String>) -> Unit,
) {
    val firstName = user.firstName.orEmpty()
    val lastName = user.lastName.orEmpty()
    val name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Unknown" }
    val birthday = user.birthday
    val ageText = birthday?.let {
        runCatching {
            val y = it.split("/").getOrNull(2)?.toInt() ?: return@let ""
            val cy = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            " , ${(cy - y)}"
        }.getOrDefault("")
    } ?: ""
    val job = user.job.orEmpty().ifBlank { "Professional model" }
    val location = user.location.orEmpty().ifBlank { "Ho Chi Minh City, Viet Nam" }
    val about = user.description.orEmpty().ifBlank { "My name is $name and I enjoy meeting new people and finding ways to help them have an uplifting experience..." }

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
    ) // :contentReference[oaicite:1]{index=1}
    val selected = user.interests.toSet()
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = AppColors.Main_Secondary1,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .offset(y = (-120).dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Spacer(Modifier.height(50.dp))

            // ===== Header: name + age + ic_location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "$name$ageText",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = job,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ic_location
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = "Location",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFFF8E6F2),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_local),
                            contentDescription = "Location",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "",
                            color = AppColors.Text_Pink,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))


            // ===== About
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = about,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!expanded) {
                Text(
                    "Read more",
                    color = AppColors.Text_Pink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { expanded = true }
                )
            }

            // ===== Interests
            Spacer(Modifier.height(24.dp))
            Text("Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allInterests.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { interest ->
                            val isSelected = selected.contains(interest.name)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AppColors.Text_Pink.copy(alpha = .12f) else Color.White.copy(alpha = .6f),
                                border = if (isSelected) BorderStroke(2.dp, AppColors.Text_Pink) else BorderStroke(1.dp, Color.LightGray.copy(.35f)),
                                tonalElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(interest.icon),
                                        contentDescription = interest.name,
                                        tint = if (isSelected) AppColors.Text_Pink else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        interest.name,
                                        color = if (isSelected) AppColors.Text_Pink else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                        repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // ===== Gallery
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "See all",
                    color = AppColors.Text_Pink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { if (images.isNotEmpty()) onSeeAll(images) }
                )
            }
            Spacer(Modifier.height(8.dp))

            if (images.isNotEmpty()) {
                val preview = images.take(6)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((preview.size + 2) / 3 * 110).dp)
                ) {
                    items(preview.size) { idx ->
                        AsyncImage(
                            model = preview[idx],
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.ic_avatar),
                            error = painterResource(R.drawable.ic_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(idx, images) }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No images available", color = AppColors.Main_Pink)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProfileHero(profile: User?, navController: NavController) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        val hero = profile?.avatarUrl ?: profile?.imageUrl?.firstOrNull()
        if (!hero.isNullOrBlank()) {
            AsyncImage(
                model = hero,
                contentDescription = "hero",
                placeholder = painterResource(R.drawable.ic_avatar),
                error = painterResource(R.drawable.ic_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_avatar),
                contentDescription = "hero-placeholder",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        IconButton(
            onClick = { navController.navigateUp() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_pink),
                contentDescription = "Location",
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun ProfileActionsRow(
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onSuperLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 52.dp)
            .offset(y = (-64).dp)
            .zIndex(2f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            icon = Icons.Filled.Close,
            background = Color.White,
            iconTint = Color.Black,
            size = 72.dp,
            shadow = 10.dp
        ) { onDislike() }
        ActionButton(
            icon = Icons.Filled.Favorite,
            background = AppColors.Text_Pink,
            iconTint = Color.White,
            size = 96.dp,
            shadow = 16.dp
        ) { onLike() }
        ActionButton(
            icon = Icons.Filled.Star,
            background = Color(0xFF4A154B),
            iconTint = Color.White,
            size = 72.dp,
            shadow = 10.dp
        ) { onSuperLike() }
    }
}
