package com.example.dating.ui.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.ui.components.BottomNavigationBar
import com.example.dating.ui.components.MessagesFilterSheet
import com.example.dating.ui.components.MessagesHeader
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.MessagesViewModel
import com.example.dating.viewmodel.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Log.d("ActivitiesDebug_uistate", uiState.toString())
    // --- NEW: Collect stories for all peers ---
    val peers = uiState.messages.map { it.peer }
//    val peerStoriesMap = remember(peers) {
//        peers.associate { peer ->
//            peer.uid to storyViewModel.observeUserStories(peer.uid)
//        }
//    }
//    val now = System.currentTimeMillis()
//    val peersWithStory = peers.filter { peer ->
//        val stories = peerStoriesMap[peer.uid]?.value ?: emptyList()
//        Log.d("ActivitiesDebug", "Peer: ${peer.uid}, stories: ${stories.map { it.id to it.expiresAt }}")
//        val hasValidStory = stories.any { it.expiresAt ?: 0 > now }
//        Log.d("ActivitiesDebug", "Peer: ${peer.uid}, hasValidStory: $hasValidStory")
//        hasValidStory
//    }
//    Log.d("ActivitiesDebug", "Peers with story: ${peersWithStory.map { it.uid }}")

    Scaffold(bottomBar = { BottomNavigationBar(navController, 2) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.MainBackground)
                .padding(paddingValues)
        ) {
            MessagesHeader(onFilterClick = { showFilterSheet = true })

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                uiState.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(uiState.error ?: "Unknown error", color = Color.Red)
                }

                else -> {
                    // Activities: Only show users with valid story
                    Text(
                        "Activities",
                        modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        // Inside your LazyRow composable
                        items(peers) { peer ->
                            val storiesState = storyViewModel.observeUserStories(peer.uid)
                            val stories by storiesState.collectAsState()
                            val hasValidStory = stories.any { it.expiresAt ?: 0 > System.currentTimeMillis() }
                            if (hasValidStory) {
                                StoryBubble(
                                    user = peer,
                                    navController = navController,
                                    storyViewModel = storyViewModel
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // White rounded container for the Messages section
                    MessagesSectionCard(
                        messages = uiState.messages,
                        onItemClick = { c ->
                            navController.navigate(
                                Screen.ChatDetail.createRoute(c.id)
                            )
                        }
                    )
                }
            }
        }

        // Filter sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = Color.White
            ) {
                MessagesFilterSheet(
                    state = filterState,
                    onChange = { viewModel.updateFilter(it) },
                    onClear = {
                        viewModel.clearFilter()
                        showFilterSheet = false
                    },
                    onApply = {
                        viewModel.applyFilter()
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MessagesSectionCard(
    messages: List<ConversationPreview>,
    onItemClick: (ConversationPreview) -> Unit
) {
    val cardHPad = 20.dp
    val avatar = 56.dp
    val gap = 12.dp
    val startIndent = cardHPad + avatar + gap

    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Messages Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "When you match and chat with others,\nyour messages will show up here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = cardHPad, end = cardHPad, top = 16.dp, bottom = 90.dp
                )
            ) {
                // Header inside the white container
                item {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(messages) { conversation ->
                    MessageItem(
                        item = conversation,
                        onClick = { onItemClick(conversation) }
                    )
                    InsetDivider(start = startIndent)
                }
            }
        }
    }
}

@Composable
fun StoryBubble(
    user: User,
    navController: NavController,
    storyViewModel: StoryViewModel
) {
    val storiesStateFlow = remember { storyViewModel.observeUserStories(user.uid) }
    val stories by storiesStateFlow.collectAsState()
    val now = System.currentTimeMillis()
    val validStories = stories.filter { it.expiresAt ?: 0 > now }
    val hasStory = validStories.isNotEmpty()
    val myUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    // True if all valid stories have been seen by current user
    val allSeen = hasStory && validStories.all { it.seenBy.contains(myUid) }

    val borderColor = when {
        !hasStory -> Color.LightGray
        allSeen -> Color.Gray // Seen color
        else -> AppColors.Text_Pink // Unseen color
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(enabled = hasStory) {
                if (hasStory) {
                    navController.navigate("story_viewer/${user.uid}")
                }
            }
    ) {
        AsyncImage(
            model = user.avatarUrl ?: "https://i.pravatar.cc/150?u=${user.uid}",
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(3.dp, borderColor, CircleShape)
        )
        Text(
            "${user.firstName} ${user.lastName}",
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MessageItem(item: ConversationPreview, onClick: () -> Unit = {}) {
    android.util.Log.d("MessageItem", item.toString())
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.peer.avatarUrl ?: "https://i.pravatar.cc/150?u=${item.peer.uid}",
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = "${item.peer.firstName} ${item.peer.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val senderName = when (item.lastMessage?.fromUid) {
                null -> ""
                item.currentUid -> "You"
                else -> "${item.peer.firstName} ${item.peer.lastName}"
            }
            val messageText = item.lastMessage?.text?.takeIf { it.isNotBlank() } ?: "No messages yet"
            Text(
                text = if (item.lastMessage != null) "$senderName: $messageText" else messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.timeAgo.isNotEmpty()) {
            Text(
                text = item.timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/** Divider bắt đầu từ chỗ text (không chạy dưới avatar) */
@Composable
fun InsetDivider(start: Dp, modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.padding(start = start),
        thickness = 0.6.dp,
        color = Color(0x1A000000) // đen 10% cho nhẹ nhàng
    )
}

/** In StoryViewModel.kt, add:
@Composable
fun StoryViewModel.observeUserStories(uid: String): State<List<com.example.dating.data.model.Story>> {
    val flow = repo.observeMyStories(uid)
    return flow.collectAsState(initial = emptyList())
}
*/
