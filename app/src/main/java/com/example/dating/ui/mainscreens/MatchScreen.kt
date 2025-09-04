package com.example.dating.ui.mainscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.MatchViewModel
import com.example.dating.viewmodel.ProfileViewModel

@Composable
fun MatchScreen(navController: NavController, matchedUserId: String?) {
    val matchViewModel: MatchViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    val currentUser by profileViewModel.user.collectAsState()
    val currentUserInfo by matchViewModel.userInfo.collectAsState()
    val matchedUserInfo by matchViewModel.matchedUserInfo.collectAsState()

    // Set current user info in MatchViewModel
    LaunchedEffect(currentUser) {
        matchViewModel.setUser(currentUser)
    }

    // Set matched user info in MatchViewModel
    LaunchedEffect(matchedUserId) {
        if (matchedUserId != null) {
            val matchedUser = matchViewModel.getUserById(matchedUserId)
            matchViewModel.setMatchedUser(matchedUser)
        }
    }
    // Save the match when both users are available
    LaunchedEffect(currentUserInfo?.uid, matchedUserInfo?.uid) {
        val uid1 = currentUserInfo?.uid
        val uid2 = matchedUserInfo?.uid
        if (uid1 != null && uid2 != null) {
            matchViewModel.saveMatch(uid1, uid2, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.GradientBackground) // Light purple
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.height(300.dp)) {

                    // Upper right profile photo (current user)
                    Box(
                        modifier = Modifier
                            .size(width = 190.dp, height = 280.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 55.dp, y = (-30).dp)
                            .rotate(10f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        if (!currentUserInfo?.avatarUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = currentUserInfo?.avatarUrl,
                                contentDescription = "Current User",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Current User",
                                tint = Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Lower left profile photo (matched user)
                    Box(
                        modifier = Modifier
                            .size(width = 190.dp, height = 280.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-55).dp, y = 90.dp)
                            .rotate(-10f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        if (!matchedUserInfo?.avatarUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = matchedUserInfo?.avatarUrl,
                                contentDescription = "Matched User",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Matched User",
                                tint = Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Heart for upper right photo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-75).dp, y = (-50).dp)
                            .rotate(10f)
                            .background(AppColors.Text_Pink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Heart for lower left photo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-60).dp, y = 120.dp)
                            .rotate(-10f)
                            .background(AppColors.Text_Pink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                }
                Spacer(modifier = Modifier.height(160.dp))
                Text(
                    text = "It's a match, ${currentUserInfo?.firstName ?: "You"}!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Example: Show matched user name
                Text(
                    text = "Matched with: ${matchedUserInfo?.firstName ?: "Unknown"}",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // You can use currentUserInfo?.avatarUrl and matchedUserInfo?.avatarUrl for images
                Text(
                    text = "Start a conversation now with each other.",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 60.dp)
                )
                Button(
                    onClick = {
                        // Find or create a conversation between current user and matched user, then navigate to chat
                        val currentUid = currentUserInfo?.uid
                        val matchedUid = matchedUserInfo?.uid
                        if (currentUid != null && matchedUid != null) {
                            navController.navigate("messages")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A154B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Say hello", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.popBackStack("home", inclusive = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Keep swiping", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
