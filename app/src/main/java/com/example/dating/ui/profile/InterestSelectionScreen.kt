package com.example.dating.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dating.R
import com.example.dating.ui.theme.AppColors
import androidx.compose.ui.text.style.TextAlign

data class Interest(
    val name: String,
    val icon: Int
)

@Composable
fun InterestSelectionScreen(navController: NavController) {
    val interests = listOf(
        Interest("Photography", R.drawable.ic_interest_photography),
        Interest("Shopping", R.drawable.ic_interest_shopping),
        Interest("Karaoke", R.drawable.ic_interest_karaoke),
        Interest("Yoga", R.drawable.ic_interest_yoga),
        Interest("Cooking", R.drawable.ic_interest_cooking),
        Interest("Tennis", R.drawable.ic_interest_tennis),
        Interest("Run", R.drawable.ic_interest_run),
        Interest("Swimming", R.drawable.ic_interest_swimming),
        Interest("Art", R.drawable.ic_interest_art),
        Interest("Traveling", R.drawable.ic_interest_travelling),
        Interest("Extreme", R.drawable.ic_interest_extreme),
        Interest("Music", R.drawable.ic_interest_music),
        Interest("Drink", R.drawable.ic_interest_drink),
        Interest("Video games", R.drawable.ic_interest_game)
    )

    val selectedInterests = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            // Back
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.Text_Pink,)
            }

            // Skip
            Text(
                text = "Skip",
                color = AppColors.Text_Pink,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { navController.navigate("search_friend") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your interests",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = AppColors.Text_Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Select a few of your interests and let everyone\nknow what you’re passionate about.",
            color = AppColors.Text_LightBlack,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()              // chiếm hết chiều ngang
                .align(Alignment.CenterHorizontally) // căn giữa trong Column
        )


        Spacer(modifier = Modifier.height(32.dp))

        // Grid of interests (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(interests) { interest ->
                val isSelected = selectedInterests.contains(interest.name)
                InterestItem(
                    interest = interest,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) selectedInterests.remove(interest.name)
                        else selectedInterests.add(interest.name)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button
        Button(
            onClick = {
                // TODO: save selectedInterests
                navController.navigate("search_friend") // Navigate to the next screen
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
fun InterestItem(interest: Interest, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AppColors.Text_Pink  else Color(0xFFF8F8F8))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(id = interest.icon),
                contentDescription = interest.name,
                tint = if (isSelected) AppColors.Main_Primary else AppColors.Main_Primary
            )
            Text(
                text = interest.name,
                color = if (isSelected) Color.White else AppColors.Main_Primary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
