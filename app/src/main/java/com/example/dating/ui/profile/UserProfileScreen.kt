package  com.example.dating.ui.profile
import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dating.data.model.Resource
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.ui.components.ProfileHero
import com.example.dating.ui.components.ProfileInfoCard
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("UnrememberedGetBackStackEntry")
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

    Box(Modifier.fillMaxSize()) {
        // Fixed back arrow at top left
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
                .zIndex(10f)

        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.example.dating.R.drawable.ic_back_pink),
                contentDescription = "Back",
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
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
                onSwipeDone = {},
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
}

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
                homeViewModel.updateEmbeddingWithFeedbackForSwipe(currentProfile, isLike)
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
        // Next profile (background)
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
                            navController = navController,
                            profile = nextProfile
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

        // Current profile (foreground, draggable)
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
                            navController = navController,
                            profile = currentProfile
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

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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