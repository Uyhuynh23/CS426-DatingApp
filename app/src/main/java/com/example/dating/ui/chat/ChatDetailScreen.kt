package com.example.dating.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.data.model.ChatMessage
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale


private val PinkPeer = Color(0xFFFFEEF6)
private val GrayMe = Color(0xFFF2F2F5)
private val Outline = Color(0xFFE7E7EA)
private val Title = Color(0xFF1C1C1E)
private val Subtle = Color(0xFF9A9AA0)

@Composable
fun ChatDetailScreen(
    conversationId: String,
    navController: NavController,
    viewModel: ChatViewModel
) {
    val peerName by viewModel.peerName.collectAsState(initial = "Grace")
    val peerAvatar by viewModel.peerAvatar.collectAsState(initial = null)
    val groupedMessages by viewModel.groupedMessages.collectAsState()
    val lastActive by viewModel.lastActive.collectAsState()
    val peerUid by viewModel.peerUid.collectAsState() // <-- Add this line

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        viewModel.loadPeer(conversationId)
        //set user as active
        viewModel.setActive(conversationId, true)
    }
    DisposableEffect(conversationId) {
        onDispose {
            // Set user as inactive when leaving the screen
            viewModel.setActive(conversationId, false)
        }
    }

    // Online detection: consider online if lastActive within 2 minutes
    val isOnline = lastActive?.let { System.currentTimeMillis() - it < 2 * 60 * 1000 } ?: false

    // đảm bảo thứ tự cũ -> mới, và tự cuộn xuống cuối
    val listState = rememberLazyListState()
    // Flatten groupedMessages to get total message count
    val totalMessages = remember(groupedMessages) { groupedMessages.sumOf { it.messages.size } }
    LaunchedEffect(totalMessages) {
        if (totalMessages > 0) {
            listState.scrollToItem(totalMessages - 1)
        }
    }


    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        ChatHeaderWhite(
            title = "Messages",
            name = peerName,
            avatarUrl = peerAvatar,
            online = isOnline,
            navController = navController,
            lastActive = lastActive,
            onBack = { navController.popBackStack() },
            onMore = { /*TODO*/ },
            peerUid = peerUid // <-- Pass peerUid here
        )

        Spacer(Modifier.height(16.dp))

        // List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            groupedMessages.forEach { dayMessages ->
                item {
                    DayDividerWhite(dayMessages.dayLabel)
                }
                items(dayMessages.messages) { msg ->
                    ChatBubble(
                        msg = msg,
                        meColor = GrayMe,
                        peerColor = PinkPeer
                    )
                }
            }
        }

        ChatInputBarWhite(
            onSend = { text ->
                if (text.isNotBlank()) {
                    viewModel.sendMessage(conversationId, text.trim())
                }
            },
            onSchedule = { /*TODO*/ },
            onVoice = { /*TODO*/ }
        )
    }
}

/* ---------------- Header ---------------- */

@Composable
private fun ChatHeaderWhite(
    title: String,
    name: String,
    avatarUrl: String?,
    online: Boolean,
    navController: NavController,
    lastActive: Long? = null,
    onBack: () -> Unit,
    onMore: () -> Unit,
    peerUid: String? = null // <-- Add this parameter
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(onClick = onBack, icon = Icons.Default.ArrowBack, tint = AppColors.Text_Pink)
            Text(
                text = title,
                color = Title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            RoundIconButton(icon = Icons.Default.Tune, onClick = onMore, tint = AppColors.Text_Pink)
        }

        Spacer(Modifier.height(6.dp))

        Row(
            Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl ?: "https://i.pravatar.cc/150?u=$name",
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        peerUid?.let {
                            navController.navigate("user_profile2/$it")
                        }
                    }
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Title
                )
                if (online) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF58D38C))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Online", color = Subtle, fontSize = 12.sp)
                    }
                } else if (lastActive != null && lastActive != 0L) {
                    Text(
                        text = "Last active ${formatTimeAgo(lastActive)}",
                        color = Subtle,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Last active unknown",
                        color = Subtle,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(lastActive: Long?): String {
    if (lastActive == null || lastActive == 0L) return "unknown"
    val now = System.currentTimeMillis()
    val diff = now - lastActive
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

/* ---------------- Day divider ---------------- */

@Composable
private fun DayDividerWhite(label: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(Modifier.weight(1f), color = Outline, thickness = 1.dp)
        Text(text = label, color = Subtle, modifier = Modifier.padding(horizontal = 12.dp))
        Divider(Modifier.weight(1f), color = Outline, thickness = 1.dp)
    }
}

/* ---------------- Bubble ---------------- */

@Composable
fun ChatBubble(
    msg: ChatMessage,
    meColor: Color,
    peerColor: Color
) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid
    val isMe = msg.fromUid == myUid

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) meColor else peerColor,
            shape = if (isMe)
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 8.dp)
            else
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 8.dp, bottomEnd = 18.dp),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier.widthIn(min = 10.dp, max = 280.dp) // Giới hạn độ rộng tối thiểu và tối đa
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = Color(0xFF1E1E1E)
            )
        }

        val time = remember(msg.timestamp) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(msg.timestamp)
        }
        Text(
            text = time,
            fontSize = 12.sp,
            color = Subtle,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .align(if (isMe) Alignment.End else Alignment.Start)
        )
    }
}

/* ---------------- Input bar ---------------- */

@Composable
private fun ChatInputBarWhite(
    onSend: (String) -> Unit,
    onSchedule: () -> Unit,
    onVoice: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Your message", color = Subtle) },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Outline,
                unfocusedBorderColor = Outline,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        RoundIconButton(Icons.Outlined.Schedule, onSchedule)
        Spacer(Modifier.width(8.dp))
        RoundIconButton(Icons.Outlined.Mic, onVoice)
        Spacer(Modifier.width(8.dp))
        RoundIconButton(
            icon = Icons.Default.Send,
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }
        )
    }
}

/* ---------------- Small helper ---------------- */

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 44.dp,
    tint: Color = AppColors.Text_Pink
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Outline),
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint)
        }
    }
}
