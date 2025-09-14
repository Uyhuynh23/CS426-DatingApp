package com.example.dating.ui.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.dating.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.dating.ui.theme.AppColors.Main_Secondary1
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun VerifyEmailScreen(
    navController: NavController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var canContinue by remember { mutableStateOf(false) }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var hasCheckedOnce by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Countdown timer effect
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            kotlinx.coroutines.delay(1000L)
            resendCountdown--
        }
    }

    // Check verification status function
    fun checkVerificationStatus() {
        if (!isCheckingVerification) {
            isCheckingVerification = true
            message = "Checking verification status..."
            coroutineScope.launch {
                try {
                    val user = authViewModel.currentUser
                    if (user != null) {
                        // Reload user to get fresh verification status
                        user.reload()
                        val isVerified = authViewModel.checkEmailVerified()
                        if (isVerified) {
                            message = "✅ Email verified successfully!"
                            canContinue = true
                        } else {
                            message = "❌ Email not verified yet. Please check your email and click the verification link."
                        }
                    } else {
                        message = "❌ Session expired. Please log in again."
                        // Navigate back to login after a delay
                        kotlinx.coroutines.delay(2000)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VerifyEmailScreen", "Error checking verification", e)
                    message = "❌ Error checking verification status. Please try again."
                } finally {
                    isCheckingVerification = false
                    hasCheckedOnce = true
                }
            }
        }
    }

    // Only listen for ON_RESUME to check verification when user returns from email app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Only auto-check if user has manually checked at least once
                    // This prevents auto-checking when screen first loads
                    if (hasCheckedOnce && !canContinue) {
                        checkVerificationStatus()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Navigate when verified with delay to show success message
    LaunchedEffect(canContinue) {
        if (canContinue) {
            kotlinx.coroutines.delay(2000)
            navController.navigate("profile") {
                popUpTo("verify_email") { inclusive = true }
            }
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
            // Email icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 24.dp),
                tint = Main_Secondary1
            )

            Text(
                text = "Verify Your Email",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "We've sent a verification link to your email address. Please check your email and click the verification link, then come back and tap 'Check Verification' below.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Status message
            message?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canContinue)
                            Color(0xFFE8F5E8)
                        else
                            Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = if (canContinue) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Loading indicator
            if (isCheckingVerification) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = Main_Secondary1
                )
            }

            // Main action buttons
            if (!canContinue) {
                // Manual verification check button - This is the main trigger button
                Button(
                    onClick = { checkVerificationStatus() },
                    enabled = !isCheckingVerification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Main_Secondary1,
                        contentColor = Color.Black,
                    )
                ) {
                    if (isCheckingVerification) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasCheckedOnce) "Check Again" else "Check Verification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Resend email button with countdown
                val canResend = resendCountdown == 0 && !isLoading && !isCheckingVerification

                OutlinedButton(
                    onClick = {
                        if (canResend) {
                            coroutineScope.launch {
                                isLoading = true
                                resendCountdown = 60 // Start 60-second countdown
                                try {
                                    val user = authViewModel.currentUser
                                    user?.sendEmailVerification()
                                    message = "📧 Verification email sent again. Please check your inbox (including spam folder)."
                                } catch (e: Exception) {
                                    message = "❌ Failed to send verification email. Please try again."
                                    resendCountdown = 0 // Reset countdown on error
                                    Log.e("VerifyEmailScreen", "Error sending verification email", e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = canResend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (canResend) Main_Secondary1 else Color.Gray,
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (canResend) Main_Secondary1 else Color.Gray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Main_Secondary1,
                            strokeWidth = 2.dp
                        )
                    } else if (resendCountdown > 0) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Resend",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resend in ${resendCountdown}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Resend",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resend Email",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            } else {
                // Continue button when verified
                Button(
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo("verify_email") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                    )
                ) {
                    Text(
                        text = "Continue to App",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Help text
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "💡 Tip: After clicking the verification link in your email, return to this app and tap 'Check Verification'",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
