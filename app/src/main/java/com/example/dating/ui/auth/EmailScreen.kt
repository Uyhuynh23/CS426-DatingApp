package com.example.dating.ui.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.dating.ui.theme.AppColors
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.viewmodel.ProfileViewModel

@Composable
fun EmailScreen(
    navController: NavController,
    onUserCreated: ((String) -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    val isSignupFailed = errorMessage != null && errorMessage != "Verification email sent. Please check your inbox."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.Text_Pink
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null // Clear error on edit
                },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isEmailFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text_Pink,
                    focusedLabelColor = AppColors.Text_Pink,
                    unfocusedBorderColor = if (isSignupFailed && !isEmailFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isSignupFailed && !isEmailFocused) Color.Red else Color.Black
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null // Clear error on edit
                },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isPasswordFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text_Pink,
                    focusedLabelColor = AppColors.Text_Pink,
                    unfocusedBorderColor = if (isSignupFailed && !isPasswordFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isSignupFailed && !isPasswordFocused) Color.Red else Color.Black
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            var startVerificationCheck by remember { mutableStateOf(false) }
            if (startVerificationCheck) {
                // Navigate to VerifyEmailScreen
                navController.navigate("verify_email")
                startVerificationCheck = false
            }
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    focusManager.clearFocus()

                    coroutineScope.launch {
                        authViewModel.checkIfEmailExists(email) { methods ->
                            if (methods.isNotEmpty()) {
                                // Email is already registered
                                isLoading = false
                                errorMessage = "This email is already used. Please log in instead."
                            } else {
                                // Proceed to sign up
                                coroutineScope.launch {
                                    val signupResult = authViewModel.signupUserWithEmailVerification(email, password)
                                    isLoading = false
                                    if (signupResult == null) {
                                        errorMessage = "Verification email sent. Please check your inbox."
                                        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                        if (uid != null) {
                                            val newUser = com.example.dating.data.model.User(uid = uid)
                                            profileViewModel.createUser(newUser)
                                        }
                                        startVerificationCheck = true
                                    } else {
                                        errorMessage = signupResult
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = isEmailValid && password.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF1FC),
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Continue")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            errorMessage?.let {
                Text(
                    text = it,
                    color = if (it.contains("success", true)) Color(0xFF4CAF50) else Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
