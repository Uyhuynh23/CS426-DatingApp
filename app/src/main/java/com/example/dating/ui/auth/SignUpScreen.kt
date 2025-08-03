package com.example.dating.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.dating.R
import androidx.compose.ui.layout.ContentScale


@Composable
fun SignUpScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .shadow(16.dp, CircleShape)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Sign up to continue",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF231942),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAC66DA),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Text(
                "Continue with email",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Divider + text giữa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(Modifier.weight(1f), color = Color(0xFFE9D6F7), thickness = 1.dp)
            Text(
                "  or sign up with  ",
                color = Color(0xFFAC66DA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(Modifier.weight(1f), color = Color(0xFFE9D6F7), thickness = 1.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Row - Sửa horizontalArrangement
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialSquareButton(
                iconRes = R.drawable.ic_facebook, // icon vuông như mẫu của bạn
                contentDescription = "Facebook",
                onClick = { /* ... */ }
            )
            SocialSquareButton(
                iconRes = R.drawable.ic_google, // icon vuông như mẫu của bạn
                contentDescription = "Google",
                onClick = { /* ... */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Terms of use",
                color = Color(0xFFAC66DA),
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* open terms link */ }
            )
            Text(
                text = "Privacy Policy",
                color = Color(0xFFAC66DA),
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* open policy link */ }
            )
        }
    }
}

@Composable
fun SocialSquareButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFE0E0E0), // xám nhạt
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )
    }
}
