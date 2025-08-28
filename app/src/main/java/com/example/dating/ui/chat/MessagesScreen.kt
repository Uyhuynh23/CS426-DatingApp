package com.example.dating.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dating.data.model.ConversationPreview
import com.example.dating.data.model.User
import com.example.dating.viewmodel.MessagesViewModel
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(viewModel: MessagesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFEEAFA))) {
        TopAppBar(title = { Text("Messages") }, actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Tune, contentDescription = null)
            }
        })

        SearchBar()

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error ?: "Unknown error occurred", color = Color.Red)
                }
            }
            uiState.messages.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet")
                }
            }
            else -> {
                Text("Activities", Modifier.padding(start = 16.dp, top = 8.dp), fontWeight = FontWeight.SemiBold)

                LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(uiState.messages) {
                        StoryAvatar(it.peer)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn {
                        items(uiState.messages) {
                            MessageItem(it)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { query = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                }
            }
        }
    )
}

@Composable
fun StoryAvatar(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        AsyncImage(
            model = user.avatarUrl ?: "https://i.pravatar.cc/150?u=${user.uid}",
            contentDescription = null,
            modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, Color.Magenta, CircleShape)
        )
        Text(user.firstName, fontSize = 12.sp)
    }
}

@Composable
fun MessageItem(item: ConversationPreview) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.peer.avatarUrl ?: "https://i.pravatar.cc/150?u=${item.peer.uid}",
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.peer.firstName, fontWeight = FontWeight.Bold)
            Text(
                if (item.isTyping) "Typing..." else item.lastMessage,
                fontSize = 14.sp,
                color = if (item.isTyping) Color.Magenta else Color.Gray
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.timeAgo, fontSize = 12.sp, color = Color.Gray)
            if (item.unreadCount > 0) {
                Badge { Text("${item.unreadCount}") }
            }
        }
    }
}
