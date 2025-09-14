package com.example.dating.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.viewmodel.StoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.data.model.Story
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.dating.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoryViewerScreen(
    uid: String,
    navController: NavController,
    storyViewModel: StoryViewModel = hiltViewModel()
) {
    val storiesState = remember { storyViewModel.observeUserStories(uid) }
    val stories by storiesState.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    val validStories = stories.filter { it.expiresAt ?: 0 > System.currentTimeMillis() }
    val currentStory = validStories.getOrNull(currentIndex)

    // Gradient like PostStoryScreen
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(AppColors.MainBackground11, AppColors.MainBackground12),
        start = Offset(0f, 0f),
        end = Offset(0f, 1000f)
    )

    LaunchedEffect(currentStory?.id) {
        if (currentStory != null) {
            storyViewModel.markSeen(uid, currentStory.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        if (currentStory == null) {
            Text(
                text = "No stories available.",
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${currentIndex + 1}/${validStories.size}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Story media
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val transform = currentStory.imageTransform
                    AsyncImage(
                        model = currentStory.mediaUrl,
                        contentDescription = "Story",
                        modifier = Modifier
                            .fillMaxSize()
                            .let { modifier ->
                                if (transform != null) {
                                    modifier.graphicsLayer(
                                        scaleX = transform.scaleX,
                                        scaleY = transform.scaleY,
                                        translationX = transform.translationX,
                                        translationY = transform.translationY,
                                        rotationZ = transform.rotationZ
                                    )
                                } else modifier
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                // Caption
                if (!currentStory.caption.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = currentStory.caption ?: "",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Timestamp
                val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }
                currentStory.createdAt?.toDate()?.let { createdAt ->
                    Text(
                        text = dateFormat.format(createdAt),
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 16.dp, end = 16.dp)
                    )
                }

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Text_Pink.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (currentIndex < validStories.size - 1) {
                                currentIndex++
                            } else {
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Text_Pink.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (currentIndex < validStories.size - 1) "Next" else "Close")
                    }
                }
            }
        }
    }
}
