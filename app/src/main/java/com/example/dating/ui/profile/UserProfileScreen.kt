package com.example.dating.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.viewmodel.UserViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.dating.R

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        user != null -> UserProfileContent(
            user = user!!,
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
            Text("Error loading profile")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileContent(
    user: User,
    onSeeAll: (List<String>) -> Unit,
    onImageClick: (Int, List<String>) -> Unit
) {
    val name = listOf(user.firstName, user.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Jessica Parker" }
    val ageText = user.birthday?.split("-")?.firstOrNull()?.toIntOrNull()?.let { birthYear ->
        val now = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        ", ${now - birthYear}"
    } ?: ", 23"

    val job = user.job ?: "Professional model"
    val location = user.location ?: "Chicago, IL, United States"
    val distance = "1 km" // mock chip giống Figma
    val aboutDefault = "My name is $name and I enjoy meeting new people and finding ways to help them have an uplifting experience. I enjoy reading..."
    val about = user.description ?: aboutDefault
    val images = user.imageUrl
    val avatar = user.avatarUrl ?: images.firstOrNull()

    // Toàn màn scroll được
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER ảnh lớn
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = avatar ?: "",
                    contentDescription = "hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(Color.LightGray)
                )

                // khu vực card trắng trượt lên + 3 nút nổi
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 300.dp) // đẩy xuống để lộ một phần ảnh
                ) {
                    Spacer(Modifier.height(48.dp)) // chừa chỗ cho dãy nút nổi (đặt ở Box bên ngoài)
                }

                // 3 nút nổi: dislike - like - star
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 280.dp)
                ) {
                    CircleButton(
                        icon = {
                            // thay bằng painterResource(R.drawable.ic_dislike) nếu em có icon XML
                            Icon(Icons.Outlined.Close, contentDescription = "dislike")
                        },
                        container = Color(0xFFFDE7EC),
                        content = Color(0xFFE34B6B)
                    )
                    CircleButton(
                        icon = { Icon(Icons.Outlined.Favorite, contentDescription = "like") },
                        container = Color(0xFFEDE2FF),
                        content = MaterialTheme.colorScheme.primary
                    )
                    CircleButton(
                        icon = { Icon(Icons.Outlined.Star, contentDescription = "star") },
                        container = Color(0xFFEDEAFF),
                        content = Color(0xFF6F57FF)
                    )
                }
            }
        }

        // CARD NỘI DUNG
        item {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp) // kéo lên ôm sát ảnh
                    .padding(horizontal = 16.dp)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {

                    // Tên + tuổi + icon location + chip distance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$name$ageText",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )

                        // icon location tròn nhạt
                        Icon(
                            painter = painterResource(R.drawable.ic_location), // có thể thay bằng Icons.Outlined.* nếu chưa có
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(job, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(distance) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // About + Read more
                    SectionTitle("About")
                    var expanded by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = about,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!expanded) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Read more")
                                }
                            },
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { expanded = true }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Interests
                    if (user.interests.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SectionTitle("Interests", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.interests.forEachIndexed { idx, interest ->
                                // Figma có chip “ticked” (màu tím) và chip thường.
                                val selected = idx < 2 // chọn 2 chip đầu làm ví dụ “ticked”
                                if (selected) {
                                    FilterChip(
                                        selected = true,
                                        onClick = {},
                                        label = { Text(interest) }
                                    )
                                } else {
                                    AssistChip(onClick = {}, label = { Text(interest) })
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Gallery dạng lưới + See all
                    if (images.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SectionTitle("Gallery", Modifier.weight(1f))
                            Text(
                                text = "See all",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable { onSeeAll(images) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        // Lưới 3 cột, cao vừa ảnh; chỉ show tối đa 6 tấm giống Figma, bấm See all để xem full
                        val preview = images.take(6)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((preview.size + 2) / 3 * 110).dp) // tính chiều cao đủ các hàng
                        ) {
                            items(preview) { img ->
                                val index = images.indexOf(img)
                                AsyncImage(
                                    model = img,
                                    contentDescription = "gallery",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onImageClick(index, images) }
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(28.dp)) } // đáy
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier
    )
}

@Composable
private fun CircleButton(
    icon: @Composable () -> Unit,
    container: Color,
    content: Color
) {
    Surface(
        color = container,
        contentColor = content,
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
