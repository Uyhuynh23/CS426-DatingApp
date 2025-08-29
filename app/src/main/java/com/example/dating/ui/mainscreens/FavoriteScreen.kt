package com.example.dating.ui.mainscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dating.viewmodel.FavoriteViewModel
import com.example.dating.ui.theme.AppColors
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.dating.data.model.User
import com.example.dating.data.model.Resource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dating.ui.components.BottomNavigationBar

@Composable
fun FavoriteScreen(navController: NavController, favoriteViewModel: FavoriteViewModel = hiltViewModel()) {
    val usersState by favoriteViewModel.usersState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, 1)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.MainBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                FavoriteHeader(navController)
                when (usersState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Resource.Failure -> {
                        val error = (usersState as Resource.Failure).exception?.message ?: "Unknown error"
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Error: $error", color = Color.Red)
                        }
                    }
                    is Resource.Success -> {
                        val users = (usersState as Resource.Success<List<User>>).result
                        if (users.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No users found", color = Color.Gray)
                            }
                        } else {
                            Text("This is a list of people who have liked you.", fontSize = 16.sp, modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp, end = 16.dp), color = Color.Black)
                            ProfileGrid(
                                profiles = users,
                                navController = navController,
                                showDelete = false,
                                moreAvailable = users.size >= 9,
                                onMoreClick = { /* TODO: Show all likedMeProfiles */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigate("profile_details") }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = AppColors.Text_Pink
            )
        }

        Text(
            text = "Matches",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        IconButton(onClick = { navController.navigate("profile_details") }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Profile",
                tint = AppColors.Text_Pink
            )
        }
    }
}

@Composable
fun ProfileGrid(
    profiles: List<User>,
    navController: NavController,
    showDelete: Boolean,
    moreAvailable: Boolean,
    onDelete: ((User) -> Unit)? = null,
    onMoreClick: () -> Unit
) {
    var showAll by remember { mutableStateOf(false) }
    val profilesToShow = if (showAll) profiles else profiles.take(6)
    val rowCount = if (showAll) (profilesToShow.size + 1) / 2 else 3
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        for (row in 0 until rowCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                for (col in 0 until 2) {
                    val idx = row * 2 + col
                    val profile = profilesToShow.getOrNull(idx)
                    if (profile != null) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF23222B))
                                .clickable {
                                    navController.navigate("profile_display/${profile.uid}")
                                }
                        ) {
                            // Profile image placeholder
                            Image(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.TopCenter)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            // Name, Age, Description
                            val name = (profile.firstName + " " + profile.lastName).trim().ifEmpty { "Unknown" }
                            val birthday = profile.birthday
                            val age = birthday?.let {
                                try {
                                    val year = it.split("/").getOrNull(2)?.toInt() ?: throw Exception("Invalid date")
                                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                    (currentYear - year).toString()
                                } catch (e: Exception) { "?" }
                            } ?: "?"
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$name, $age", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, maxLines = 1)
                            }
                            if (showDelete && onDelete != null) {
                                IconButton(onClick = { onDelete(profile) }, modifier = Modifier.align(Alignment.TopEnd)) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (!showAll && moreAvailable) {
            Button(onClick = { showAll = true }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)) {
                Text("More")
            }
        }
        if (showAll && profiles.size > 6) {
            Button(onClick = { showAll = false }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)) {
                Text("Show Less")
            }
        }
    }
}
