package com.example.dating.ui.profile
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dating.data.model.Resource
import com.example.dating.navigation.Screen
import com.example.dating.ui.components.ProfileHero
import com.example.dating.ui.components.ProfileInfoCard
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.UserViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun UserProfileScreen2(
    navController: NavController,
    userUid: String? = null,
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Observe user profile using UserViewModel
    LaunchedEffect(userUid) {
        userViewModel.observeUser(userUid)
    }
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val images = user?.imageUrl ?: emptyList()

    Box(Modifier.fillMaxSize()) {
        // Fixed back arrow at top left
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
                .zIndex(10f)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.example.dating.R.drawable.ic_back_pink),
                contentDescription = "Back",
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...", color = Color.Gray)
            }
            user == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found", color = Color.Gray)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().background(AppColors.MainBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    ProfileHero(
                        navController=navController,
                        profile = user!!
                    )
                }
                item { Spacer(modifier = Modifier.padding(32.dp)) }
                item {
                    ProfileInfoCard(
                        user = user!!,
                        images = images,
                        onSeeAll = { imgs ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("images", ArrayList(imgs))
                            navController.navigate(Screen.PhotoViewer.route(0))
                        },
                        onImageClick = { index, imgs ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("images", ArrayList(imgs))
                            navController.navigate(Screen.PhotoViewer.route(index))
                        }
                    )
                }
            }
        }
    }
}