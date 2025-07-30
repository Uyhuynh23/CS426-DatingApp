package com.example.dating.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyCodeScreen(navController: NavController) {
    var otp by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(42) }
    val coroutineScope = rememberCoroutineScope()

    // Countdown Timer
    LaunchedEffect(Unit) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Back Button align left
        Box(
            modifier = Modifier
                .fillMaxWidth() // container full width
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8))
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart), // align left
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timer
        Text(
            text = String.format("00:%02d", timer),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B0A2B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Type the verification code\nwe’ve sent you",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP Boxes
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                val char = otp.getOrNull(index)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (char.isNotEmpty()) Color(0xFFFFF1FC)
                            else Color(0xFFF8F8F8)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B0A2B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Custom Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "←")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .clickable(enabled = key.isNotEmpty()) {
                                when (key) {
                                    "←" -> if (otp.isNotEmpty()) otp = otp.dropLast(1)
                                    else -> if (otp.length < 4) otp += key
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "←" -> Icon(
                                Icons.Default.Backspace,
                                contentDescription = "Delete"
                            )
                            "" -> {}
                            else -> Text(
                                text = key,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resend
        Text(
            text = "Send again",
            color = Color(0xFFD44BDB),
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                timer = 42
                otp = ""
                coroutineScope.launch {
                    // TODO: gọi API resend OTP
                }
            }
        )
    }
}
