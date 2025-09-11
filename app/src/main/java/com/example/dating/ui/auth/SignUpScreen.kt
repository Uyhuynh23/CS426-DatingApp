package com.example.dating.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dating.R
import com.example.dating.data.model.Resource
import com.example.dating.navigation.Screen
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.AuthViewModel
import com.example.dating.viewmodel.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import com.example.dating.data.model.User

@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // --- Observe auth states ---
    val googleState by authViewModel.googleSignInFlow.collectAsState()
    val facebookState by authViewModel.facebookSignInFlow.collectAsState()
    val uid = authViewModel.currentUser?.uid

    // Correct way to collect userState from Flow<User?>


    // --- Google launcher ---
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken.isNullOrEmpty()) {
                Toast.makeText(context, "Failed to get Google token", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.signupWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Facebook callback manager + registration ---
    val callbackManager = LocalFacebookCallbackManager.current

    LaunchedEffect(Unit) {
        com.facebook.login.LoginManager.getInstance().registerCallback(
            callbackManager,
            object : com.facebook.FacebookCallback<com.facebook.login.LoginResult> {
                override fun onSuccess(result: com.facebook.login.LoginResult) {
                    val token = result.accessToken.token
                    authViewModel.signupWithFacebook(token)
                }

                override fun onCancel() {
                    Toast.makeText(context, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: com.facebook.FacebookException) {
                    Toast.makeText(context, "Facebook login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // --- React to auth results (both providers) ---

    //GOOGLE AUTH
    LaunchedEffect(googleState) {
        when (val state = googleState) {
            is Resource.Success -> {
                val uid = authViewModel.currentUser?.uid
                if (uid != null) {
                    authViewModel.getUser(uid).collect { user ->
                        if (user != null) {
                            val hasData = listOf(
                                user.firstName, user.lastName, user.birthday, user.gender,
                                user.job, user.location, user.description, user.avatarUrl
                            ).any { it != null && it.toString().isNotBlank() }
                            val hasInterests = user.interests?.isNotEmpty() == true
                            val hasImages = user.imageUrl?.isNotEmpty() == true

                            if (hasData || hasInterests || hasImages) {
                                authViewModel.logout(profileViewModel)
                                profileViewModel.clearUser()
                                Toast.makeText(context, "This account already exists. Please log in.", Toast.LENGTH_LONG).show()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                                Log.d("SignUpScreen", "Google user data already exists, logging out")
                                return@collect
                            }
                        }

                        navController.navigate("profile") {
                            Log.d("SignUpScreen", "Navigating to profile (Google)")
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            }

            is Resource.Failure -> {
                Toast.makeText(context, state.exception.message ?: "Google auth failed", Toast.LENGTH_SHORT).show()
            }

            is Resource.Loading, null -> Unit
        }
    }

    //FACEBOOK AUTH
    LaunchedEffect(facebookState) {
        when (val state = facebookState) {
            is Resource.Success -> {
                val uid = authViewModel.currentUser?.uid
                if (uid != null) {
                    authViewModel.getUser(uid).collect { user ->
                        if (user != null) {
                            val hasData = listOf(
                                user.firstName, user.lastName, user.birthday, user.gender,
                                user.job, user.location, user.description, user.avatarUrl
                            ).any { it != null && it.toString().isNotBlank() }
                            val hasInterests = user.interests?.isNotEmpty() == true
                            val hasImages = user.imageUrl?.isNotEmpty() == true

                            if (hasData || hasInterests || hasImages) {
                                authViewModel.logout(profileViewModel)
                                profileViewModel.clearUser()
                                Toast.makeText(context, "This account already exists. Please log in.", Toast.LENGTH_LONG).show()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                                Log.d("SignUpScreen", "Facebook user data already exists, logging out")
                                return@collect
                            }
                        }

                        navController.navigate("profile") {
                            Log.d("SignUpScreen", "Navigating to profile (Facebook)")
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            }

            is Resource.Failure -> {
                Toast.makeText(context, state.exception.message ?: "Facebook auth failed", Toast.LENGTH_SHORT).show()
            }

            is Resource.Loading, null -> Unit
        }
    }



    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp).shadow(16.dp, CircleShape)
        )

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Sign up to continue",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF231942),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Email
        Button(
            onClick = { navController.navigate(Screen.EmailScreen.route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFF1FC),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Continue with email") }

        Spacer(Modifier.height(16.dp))

        // Phone
        Button(
            onClick = { navController.navigate("phone_number") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A154B),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Use phone number") }

        Spacer(Modifier.height(36.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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

        Spacer(Modifier.height(24.dp))

        // Social buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialSquareButton(
                iconRes = R.drawable.ic_facebook,
                contentDescription = "Facebook",
                onClick = {
                    // Ensure you’ve added your App ID/Client Token + manifest setup already
                    LoginManager.getInstance().logOut() // optional: clear any stale session
                    LoginManager.getInstance().logInWithReadPermissions(
                        /* activity = */ androidx.activity.ComponentActivity::class.java
                            .cast(context as? androidx.activity.ComponentActivity) ?: return@SocialSquareButton,
                        listOf("email", "public_profile")
                    )
                },
                isLoading = facebookState is Resource.Loading
            )
            SocialSquareButton(
                iconRes = R.drawable.ic_google,
                contentDescription = "Google",
                onClick = {
                    authViewModel.performGoogleSignIn(context) { intent ->
                        googleLauncher.launch(intent)
                    }
                },
                isLoading = googleState is Resource.Loading
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Terms of use", color = AppColors.Main_Primary, fontSize = 14.sp,
                modifier = Modifier.clickable { /* open terms */ })
            Text("Privacy Policy", color = AppColors.Main_Primary, fontSize = 14.sp,
                modifier = Modifier.clickable { /* open policy */ })
        }
    }

    // Add this logic before calling signupUser in your email/phone sign up flow:
    // (Assume you have email and/or phone input)
    // Example for email:
    /*
    val userExists by viewModel.userRepository.getUserByEmail(email).collectAsState(initial = null)
    if (userExists != null) {
        Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show()
        return // Stop sign up process
    } else {
        viewModel.signupUser(name, email, password)
    }
    */
    // You may need to implement getUserByEmail in UserRepository if not present.
}

@Composable
private fun SocialSquareButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFE0E0E0),
    cornerRadius: Dp = 16.dp,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius))
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
