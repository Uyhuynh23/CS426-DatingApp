package com.example.dating.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun GenderSelectionScreen(navController: NavController) {
    var selectedGender by remember { mutableStateOf("Man") } // mặc định chọn Man

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header: Back + Skip
        Box(modifier = Modifier.fillMaxWidth()) {
            // Back
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8))
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            // Skip
            Text(
                text = "Skip",
                color = Color(0xFFD44BDB),
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { navController.navigate("interest_select") }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = "I am a",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(90.dp))

        // Gender Options
        // Gender Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // padding so với background màn hình
        ) {
            GenderOption(
                label = "Woman",
                isSelected = selectedGender == "Woman",
                onClick = { selectedGender = "Woman" }
            )
            Spacer(modifier = Modifier.height(16.dp))
            GenderOption(
                label = "Man",
                isSelected = selectedGender == "Man",
                onClick = { selectedGender = "Man" }
            )
            Spacer(modifier = Modifier.height(16.dp))
            GenderOption(
                label = "Choose another",
                isSelected = selectedGender == "Other",
                onClick = { selectedGender = "Other" },
                showArrow = true
            )
        }


        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = {
                // TODO: Save gender và chuyển sang màn tiếp theo
                navController.navigate("interest_select")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1FC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Continue",
                color = Color(0xFF2B0A2B),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GenderOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFFFF1FC) else Color(0xFFF8F8F8))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF2B0A2B) else Color.Black
        )

        when {
            isSelected -> Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF2B0A2B))
            showArrow -> Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow", tint = Color.Gray)
        }
    }
}
