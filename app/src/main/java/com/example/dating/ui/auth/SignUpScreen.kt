package com.example.dating.ui.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.dating.R
import com.example.dating.ui.theme.AppColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Google Sign-In configuration
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleSignIn", "Sign in successful!")
            Log.d("GoogleSignIn", "User name: ${account.displayName}")
            Log.d("GoogleSignIn", "User email: ${account.email}")

            // Navigate to Home screen after successful sign in
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign in failed with code: ${e.statusCode}")
            Log.e("GoogleSignIn", "Error message: ${e.message}")
        }
    }

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
                containerColor = AppColors.Main_Secondary1,
                contentColor = AppColors.Main_Primary
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
            Divider(Modifier.weight(1f), color = AppColors.Text_LightBlack, thickness = 0.5.dp)
            Text(
                "  or sign up with  ",
                color = AppColors.Main_Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(Modifier.weight(1f), color = AppColors.Text_LightBlack, thickness = 0.5.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialSquareButton(
                iconRes = R.drawable.ic_facebook,
                contentDescription = "Facebook",
                onClick = { /* Facebook sign in */ }
            )
            SocialSquareButton(
                iconRes = R.drawable.ic_google,
                contentDescription = "Google",
                onClick = { signInWithGoogle(context, googleSignInLauncher) }
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
                color = AppColors.Main_Primary,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* open terms link */ }
            )
            Text(
                text = "Privacy Policy",
                color = AppColors.Main_Primary,
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
    borderColor: Color = Color(0xFFE0E0E0),
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

// Helper function for Google Sign-In
private fun signInWithGoogle(
    context: Context,
    launcher: ActivityResultLauncher<Intent>
) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken("224972776925-jcghkv22uojd0ka97ag7ebqn8rkuu0gl.apps.googleusercontent.com")
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Sign out first to ensure account picker shows
    googleSignInClient.signOut().addOnCompleteListener {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
}