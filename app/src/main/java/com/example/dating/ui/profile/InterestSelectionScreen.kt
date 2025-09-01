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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.viewmodel.ProfileViewModel
import com.example.dating.data.model.Resource
import com.example.dating.data.model.Interest
import com.example.dating.data.model.ALL_INTERESTS

@Composable
fun InterestSelectionScreen(navController: NavController) {
    val interests = ALL_INTERESTS

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val user by profileViewModel.user.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()

    val selectedInterests = remember { mutableStateListOf<String>().apply {
        user?.interests?.forEach { add(it) }
    } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (updateState) {
            is Resource.Loading -> {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is Resource.Failure -> {
                val exception = (updateState as Resource.Failure).exception
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = exception.localizedMessage ?: "Update failed.",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
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
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
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

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        profileViewModel.updateInterests(selectedInterests.toList())
                        navController.navigate("search_friend")
                    },
                    enabled = selectedInterests.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Main_Secondary1,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "Confirm",
                        color = AppColors.Main_Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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
