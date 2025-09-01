package com.example.dating.ui.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.R
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userUid: String? = null,
    viewModel: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(userUid) { viewModel.observeUser(userUid) }

    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            user != null -> UserProfileContent(
                modifier = Modifier.padding(padding),
                user = user!!,
                onSwipeDone = { dir ->
                    // báo về Home để tự next
                    navController.previousBackStackEntry?.savedStateHandle?.set("advanceNext", user!!.uid)
                    navController.previousBackStackEntry?.savedStateHandle?.set("swipeDir", dir)
                    navController.navigateUp()
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
            else -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cannot load profile")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileContent(
    modifier: Modifier = Modifier,
    user: User,
    onSwipeDone: (dir: String) -> Unit,
    onSeeAll: (List<String>) -> Unit,
    onImageClick: (Int, List<String>) -> Unit
) {
    // ====== Safe values + defaults
    val name = listOf(user.firstName, user.lastName)
        .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Jessica Parker" }
    val ageText = user.birthday?.let {
        // Try to parse year from yyyy-MM-dd or dd/MM/yyyy or MM/dd/yyyy
        val year = when {
            it.contains("-") -> it.split("-").firstOrNull()?.toIntOrNull() // yyyy-MM-dd
            it.contains("/") -> it.split("/").lastOrNull()?.toIntOrNull() // dd/MM/yyyy or MM/dd/yyyy
            else -> null
        }
        year?.let { birthYear ->
            ", " + (java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - birthYear)
        } ?: ", ?"
    } ?: ", ?"
    val job = user.job ?: "Professional model"
    val location = user.location ?: "Chicago, IL, United States"
    val about = user.description ?: "My name is $name and I enjoy meeting new people and finding ways to help them have an uplifting experience. I enjoy reading..."
    val images = user.imageUrl
    val avatarUrl = user.avatarUrl ?: images.firstOrNull() // có thể null

    // ====== Swipe state (hero)
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val threshold = 200f
    val rotation = (offsetX.value / 15).coerceIn(-25f, 25f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ----------------- HERO (swipeable) -----------------
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                    .graphicsLayer { rotationZ = rotation }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        offsetX.value > threshold -> {
                                            offsetX.animateTo(1000f, tween(280))
                                            onSwipeDone("like")
                                            offsetX.snapTo(0f); offsetY.snapTo(0f)
                                        }
                                        offsetX.value < -threshold -> {
                                            offsetX.animateTo(-1000f, tween(280))
                                            onSwipeDone("dislike")
                                            offsetX.snapTo(0f); offsetY.snapTo(0f)
                                        }
                                        else -> {
                                            offsetX.animateTo(0f, tween(220))
                                            offsetY.animateTo(0f, tween(220))
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
                // Ảnh hero: có URL thì AsyncImage, không có thì avatar mặc định
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
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

                // 3 nút nổi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 280.dp)
                ) {
                    CircleButton({ Icon(Icons.Outlined.Close, null) }, Color(0xFFFDE7EC), Color(0xFFE34B6B))
                    CircleButton({ Icon(Icons.Outlined.Favorite, null) }, Color(0xFFEDE2FF), MaterialTheme.colorScheme.primary)
                    CircleButton({ Icon(Icons.Outlined.Star, null) }, Color(0xFFEDEAFF), Color(0xFF6F57FF))
                }
            }
        }

        // ----------------- CARD nội dung (kéo được) -----------------
        item {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {

                    // Tên + tuổi
                    Text("$name$ageText", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))

                    // Job
                    Text(job, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))

                    // Location + 1km
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        AssistChip(onClick = {}, label = { Text("1 km") })
                    }

                    // About
                    Spacer(Modifier.height(16.dp))
                    Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        about,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!expanded) {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Read more")
                                }
                            },
                            modifier = Modifier.padding(top = 6.dp).clickable { expanded = true }
                        )
                    }

                    // Interests
                    if (user.interests.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.interests.forEachIndexed { i, itx ->
                                if (i < 2) FilterChip(selected = true, onClick = {}, label = { Text(itx) })
                                else AssistChip(onClick = {}, label = { Text(itx) })
                            }
                        }
                    }

                    // Gallery
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(
                            "See all",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable {
                                if (images.isNotEmpty()) onSeeAll(images)
                            }
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
                            items(preview) { img ->
                                val idx = images.indexOf(img)
                                AsyncImage(
                                    model = img,
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
                        // Empty state gallery
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No images available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(28.dp)) }
    }
}

@Composable
private fun CircleButton(icon: @Composable () -> Unit, container: Color, content: Color) {
    Surface(
        color = container,
        contentColor = content,
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = Modifier.size(56.dp)
    ) { Box(contentAlignment = Alignment.Center) { icon() } }
}
