package com.example.dating.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.viewmodel.StoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.data.model.Story
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

    // Mark as seen when story is shown
    LaunchedEffect(currentStory?.id) {
        if (currentStory != null) {
            storyViewModel.markSeen(uid, currentStory.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (currentStory == null) {
            // No stories to show
            Text(
                text = "No stories available.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Story content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar: Close button and progress
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${currentIndex + 1}/${validStories.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Story media
                AsyncImage(
                    model = currentStory.mediaUrl,
                    contentDescription = "Story",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Caption and time
                if (!currentStory.caption.isNullOrBlank()) {
                    Text(
                        text = currentStory.caption ?: "",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }
                val createdAt = currentStory.createdAt?.toDate()
                if (createdAt != null) {
                    Text(
                        text = dateFormat.format(createdAt),
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0
                    ) { Text("Prev") }
                    Button(
                        onClick = { if (currentIndex < validStories.size - 1) currentIndex++ else navController.popBackStack() },
                        enabled = currentIndex < validStories.size - 1
                    ) { Text(if (currentIndex < validStories.size - 1) "Next" else "Close") }
                }
            }
        }
    }
}