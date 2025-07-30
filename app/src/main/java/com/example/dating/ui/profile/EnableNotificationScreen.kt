package com.example.dating.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dating.R

@Composable
fun EnableNotificationScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Skip button
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Skip",
                color = Color(0xFFD44BDB),
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        // TODO: handle skip, tạm thời về home
                        navController.navigate("home")
                    }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Illustration
        Image(
            painter = painterResource(id = R.drawable.ic_notification_placeholder),
            contentDescription = "Notification Illustration",
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = "Enable notification’s",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Get push-notification when you get the match\nor receive a message.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Enable Button
        Button(
            onClick = {
                // TODO: Yêu cầu permission POST_NOTIFICATIONS (Android 13+)
                navController.navigate("home")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1FC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "I want to be notified",
                color = Color(0xFF2B0A2B),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


