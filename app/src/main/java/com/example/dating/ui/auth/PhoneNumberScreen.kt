package com.example.dating.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hbb20.CountryCodePicker
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PhoneNumberScreen(navController: NavController) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") } // Default to India

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "My mobile",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please enter your valid phone number. We will send you a 4-digit code to verify your account.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    CountryCodePicker(ctx).apply {
                        setDefaultCountryUsingNameCode("IND")
                        setAutoDetectedCountry(true)
                        setOnCountryChangeListener {
                            countryCode = "+${selectedCountryCode}"
                        }
                    }
                },
                modifier = Modifier.width(140.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text("331 623 8413") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate("verify")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1FC)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

