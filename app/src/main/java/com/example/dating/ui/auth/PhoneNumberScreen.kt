package com.example.dating.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dating.R

data class CountryCode(
    val flagRes: Int,
    val code: String,
    val name: String
)

@Composable
fun PhoneNumberScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember {
        mutableStateOf(
            CountryCode(R.drawable.ic_google, "+1", "United States")
        )
    }

    val countries = listOf(
        CountryCode(R.drawable.ic_facebook, "+1", "United States"),
        CountryCode(R.drawable.ic_google, "+84", "Vietnam"),
        CountryCode(R.drawable.ic_apple, "+81", "Japan")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = "My mobile",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Please enter your valid phone number. We will send you a 4-digit code to verify your account.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Phone input field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp)
        ) {
            // Country dropdown trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = true }
            ) {
                Image(
                    painter = painterResource(id = selectedCountry.flagRes),
                    contentDescription = "Flag",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = selectedCountry.code, fontSize = 16.sp, color = Color.Black)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.flagRes} ${country.name} (${country.code})") },
                        onClick = {
                            selectedCountry = country
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Phone number input
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

        // Continue button
        Button(
            onClick = {
                navController.navigate("verify_code") // Chuyển sang tab khác
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


