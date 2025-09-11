package com.example.dating.ui.profile

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.dating.ui.theme.AppColors

@Composable
fun EnableNotificationScreen(navController: NavController) {
    // Permission launcher for notification
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Điều hướng đến home bất kể quyền có được cấp hay không
            // Bạn có thể xử lý logic khác ở đây nếu cần (ví dụ: lưu trạng thái permission)
            navController.navigate("home")
        }
    )

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
            // Back
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.Text_Pink,)
            }

            // Skip
            Text(
                text = "Skip",
                color = AppColors.Text_Pink,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { navController.navigate("home") }
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
            text = "Enable notification's",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = AppColors.Text_Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Get push-notification when you get the match\nor receive a message.",
            color = AppColors.Text_LightBlack,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Enable Button
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Yêu cầu quyền thông báo cho Android 13+ (API level 33+)
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Đối với Android < 13, không cần yêu cầu quyền thông báo
                    navController.navigate("home")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1FC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "I want to be notified",
                color = AppColors.Main_Primary,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
