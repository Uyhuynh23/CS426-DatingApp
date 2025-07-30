package com.example.dating.ui.auth


import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.hilt.navigation.compose.hiltViewModel

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException

@SuppressLint("UnrememberedMutableState")
@Composable
fun PhoneNumberScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }

    val isPhoneValid by derivedStateOf {
        try {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val regionCode = phoneUtil.getRegionCodeForCountryCode(countryCode.replace("+", "").toInt())
            val numberProto = phoneUtil.parse(phoneNumber, regionCode)
            phoneUtil.isValidNumber(numberProto)
        } catch (e: Exception) {
            false
        }
    }


    // Observe verificationId to trigger navigation
    val verificationId by viewModel.verificationId.collectAsState()

    LaunchedEffect(verificationId) {
        if (verificationId != null) {
            navController.navigate("verify_code")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text("My mobile", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter your valid phone number. We will send you an OTP.",
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
                        setDefaultCountryUsingNameCode("IN")
                        setAutoDetectedCountry(true)
                        setOnCountryChangeListener {
                            countryCode = "+${selectedCountryCode}"
                        }
                    }
                },
                modifier = Modifier.width(100.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text("331 623 8413") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                val fullNumber = countryCode + phoneNumber
                viewModel.sendOtp(fullNumber,activity)

            },
            enabled = isPhoneValid,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1FC)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", color = Color.Black, fontSize = 16.sp)
        }
    }
}
