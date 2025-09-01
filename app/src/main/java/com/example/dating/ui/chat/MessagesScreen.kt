package com.example.dating.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.navigation.Screen
import com.example.dating.ui.components.*
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.MessagesViewModel
import com.example.dating.viewmodel.StoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel() // <-- Add StoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }



    Scaffold(bottomBar = { BottomNavigationBar(navController, 2) }) { paddingValues ->
        LaunchedEffect(Unit) {
            viewModel.loadMessages()
        }
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
                    // Activities
                    Text(
                        "Activities",
                        modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(uiState.messages) { conversation ->
                            StoryBubble(
                                user = conversation.peer,
                                navController = navController,
                                storyViewModel = storyViewModel
                            )
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
    val hasStory = stories.any { it.expiresAt ?: 0 > System.currentTimeMillis() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(enabled = hasStory) {
                if (hasStory) {
                    // Use navController.navigate with a defined Screen route for story viewing
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
                .border(
                    3.dp,
                    if (hasStory) AppColors.Text_Pink else Color.LightGray,
                    CircleShape
                )
        )
        Text(user.firstName, fontSize = 12.sp)
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
