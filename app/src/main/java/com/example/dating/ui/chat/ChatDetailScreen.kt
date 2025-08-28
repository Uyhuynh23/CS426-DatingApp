package com.example.dating.ui.chat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dating.data.model.ChatMessage
import com.example.dating.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
        TopAppBar(
            title = { Text("Messages") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
            }
        )

        Divider()

        // Messages list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = true // để tin nhắn mới ở cuối
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Input
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var text by remember { mutableStateOf("") }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Your message") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            IconButton(onClick = {
                if (text.isNotBlank()) {
                    viewModel.sendMessage(conversationId, text)
                    text = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = null)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isMe = msg.fromUid == FirebaseAuth.getInstance().currentUser?.uid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isMe) Color(0xFFDCF8C6) else Color.White,
            modifier = Modifier.padding(4.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                Text(msg.text)
                Text(
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(msg.timestamp),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
