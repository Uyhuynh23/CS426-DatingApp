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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel?, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authResource = viewModel?.loginFlow?.collectAsState()
    val isLoginFailed = authResource?.value is Resource.Failure
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var loginClicked by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current


    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
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
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.headlineLarge.fontSize),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isEmailFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor =  AppColors.Text_Pink,
                    focusedLabelColor =  AppColors.Text_Pink,
                    unfocusedBorderColor = if (isLoginFailed && !isEmailFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isLoginFailed && !isEmailFocused) Color.Red else Color.Black
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                    unfocusedBorderColor = if (isLoginFailed && !isPasswordFocused) Color.Red else Color.LightGray,
                    unfocusedLabelColor = if (isLoginFailed && !isPasswordFocused) Color.Red else Color.Black
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Button(
                onClick = {
                    loginClicked = true
                    viewModel?.loginUser(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Main_Secondary1,
                    contentColor = Color.Black,
                )
            ) {
                Text("Login", style = MaterialTheme.typography.titleMedium)
            }

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
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.Text_Pink
                )
            }

            if (loginClicked) {
                authResource?.value?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        when (it) {
                            is Resource.Failure -> {
                                // Clear focus on failure
                                LaunchedEffect(Unit) {
                                    isEmailFocused = false
                                    isPasswordFocused = false
                                    focusManager.clearFocus()
                                    loginClicked = false
                                }
                                Text(
                                    text = it.exception.message ?: "Login failed",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                            is Resource.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            is Resource.Success -> {
                                LaunchedEffect(Unit) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    loginClicked = false

                                }
                            }
                        }
                    }
                }
            }
        }


    }
}
