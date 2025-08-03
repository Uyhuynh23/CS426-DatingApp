package com.example.dating.ui.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.dating.ui.theme.AppColors.Main_Secondary1

@Composable
fun VerifyEmailScreen(
    navController: NavController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var canContinue by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Periodically check verification status every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            val user = authViewModel.currentUser
            if (user != null) {
                val isVerified = authViewModel.checkEmailVerified()
                if (isVerified) {
                    message = "Email verified successfully!"
                    canContinue = true
                    break
                } else {
                    message = "Please verify your email."
                }
            } else {
                message = "Please log in again."
                break
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    // When verified, navigate or popBackStack as needed
    LaunchedEffect(canContinue) {
        if (canContinue) {
            // Example: navigate to next screen or pop
            // navController.navigate("next_screen")
            // or navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verify Your Email",
                fontSize = 28.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            message?.let {
                Text(
                    text = it,
                    color = if (canContinue) Color(0xFF4CAF50) else Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            if (!canContinue) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val user = authViewModel.currentUser
                            user?.sendEmailVerification()
                            isLoading = false
                            message = "Verification email sent again. Please check your inbox."
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Main_Secondary1,
                        contentColor = Color.Black,
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Resend Verification Email")
                    }
                }
            } else {
                Button(
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo("verify_email") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Main_Secondary1,
                        contentColor = Color.Black,
                    )
                ) {
                    Text("Continue")
                }
            }
        }
    }
}
