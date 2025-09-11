import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dating.data.model.Resource
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.AuthViewModel
import com.example.dating.ui.theme.AppColors.Main_Secondary1
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import com.example.dating.ui.auth.LocalFacebookCallbackManager
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var loginClicked by remember { mutableStateOf(false) }

    // Observe state from ViewModel
    val authResource = viewModel.loginFlow.collectAsState()
    val googleState = viewModel.googleSignInFlow.collectAsState().value
    val facebookState = viewModel.facebookSignInFlow.collectAsState().value

    val isLoggedOut = authResource.value is Resource.Failure &&
            (authResource.value as? Resource.Failure)?.exception?.message == "User logged out"
    val isLoginFailed = authResource.value is Resource.Failure &&
            (authResource.value as? Resource.Failure)?.exception?.message != "User logged out"

    // Google launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                viewModel.signupWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Failed to get Google token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Facebook callback setup
    val callbackManager = LocalFacebookCallbackManager.current

    LaunchedEffect(Unit) {
        com.facebook.login.LoginManager.getInstance().registerCallback(
            callbackManager,
            object : com.facebook.FacebookCallback<com.facebook.login.LoginResult> {
                override fun onSuccess(result: com.facebook.login.LoginResult) {
                    val token = result.accessToken.token
                    viewModel.signupWithFacebook(token)
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


    // Navigate on success (Google or Facebook)
    LaunchedEffect(googleState, facebookState) {
        val state = googleState ?: facebookState
        when (state) {
            is Resource.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }

            is Resource.Failure -> {
                Toast.makeText(context, state.exception.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }

            is Resource.Loading, null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isEmailFocused = it.isFocused },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text_Pink,
                    focusedLabelColor = AppColors.Text_Pink,
                    unfocusedBorderColor = if (isLoginFailed && !isEmailFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isLoginFailed && !isEmailFocused) Color.Red else Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isPasswordFocused = it.isFocused },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text_Pink,
                    focusedLabelColor = AppColors.Text_Pink,
                    unfocusedBorderColor = if (isLoginFailed && !isPasswordFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isLoginFailed && !isPasswordFocused) Color.Red else Color.Black
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            // Email/password login
            Button(
                onClick = {
                    loginClicked = true
                    viewModel.loginUser(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Main_Secondary1,
                    contentColor = Color.Black,
                )
            ) {
                Text("Login", style = MaterialTheme.typography.titleMedium)
            }

            // Google Sign-In button
            Button(
                onClick = {
                    viewModel?.performGoogleSignIn(context) { intent ->
                        googleSignInLauncher.launch(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.dating.R.drawable.ic_google),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Google")
            }

            // Facebook Sign-In button

            Button(
                onClick = {
                    LoginManager.getInstance().logOut()
                    LoginManager.getInstance().logInWithReadPermissions(
                        context as? androidx.activity.ComponentActivity ?: return@Button,
                        listOf("email", "public_profile")
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.dating.R.drawable.ic_facebook),
                    contentDescription = "Facebook",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Facebook")
            }
            // Redirect to Sign Up
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurface)
                Text("Sign up", fontWeight = FontWeight.Bold, color = AppColors.Text_Pink)
            }

            // Show login feedback
            if (loginClicked) {
                authResource.value.let {
                    when (it) {
                        is Resource.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        is Resource.Failure -> {
                            LaunchedEffect(Unit) {
                                loginClicked = false
                                focusManager.clearFocus()
                            }
                            Text(
                                text = it.exception.message ?: "Login failed",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is Resource.Success -> {
                            LaunchedEffect(Unit) {
                                if (!isLoggedOut) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    loginClicked = false
                                }
                            }
                        }
                        else -> { /* No-op */}
                    }
                }
            }
        }
    }
}
