package com.example.dating.ui.profile


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dating.data.model.Resource
import com.example.dating.navigation.Screen
import com.example.dating.ui.components.ProfileHero
import com.example.dating.ui.components.ProfileInfoCard
import com.example.dating.ui.theme.AppColors
import com.example.dating.viewmodel.FavoriteViewModel

data class Interest(
    val name: String,
    val icon: Int
)


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen2(
    navController: NavController,
    userUid: String? = null,
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val usersResource by favoriteViewModel.usersState.collectAsState()
    val profiles = usersResource.let { res ->
        when (res) {
            is Resource.Success -> res.result
            else -> emptyList()
        }
    }
    val user = profiles.find { it.uid == userUid }
    val images = user?.imageUrl ?: emptyList()

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not found", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppColors.MainBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ProfileHero(
                profile = user,
                navController = navController
            )
        }
        item{ Spacer(modifier = Modifier.padding(32.dp) ) }
        item {
            ProfileInfoCard(
                user = user,
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


/*@Composable
private fun ActionButton(
    icon: ImageVector,
    background: Color,
    iconTint: Color,
    size: Dp,
    shadow: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(shadow, CircleShape, clip = false)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

private fun computeAge(birthday: String?): Int? {
    if (birthday.isNullOrBlank()) return null
    return try {
        val year = when {
            birthday.contains("/") -> birthday.split("/").getOrNull(2)?.toInt()
            birthday.contains("-") -> birthday.split("-").getOrNull(0)?.toInt()
            else -> null
        } ?: return null
        Calendar.getInstance().get(Calendar.YEAR) - year
    } catch (_: Exception) { null }
}*/

/*
@Composable
fun ProfileActionsRow(
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onSuperLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 52.dp)
            .offset(y = (-64).dp)
            .zIndex(2f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            icon = Icons.Filled.Close,
            background = Color.White,
            iconTint = Color.Black,
            size = 72.dp,
            shadow = 10.dp
        ) { onDislike() }
        ActionButton(
            icon = Icons.Filled.Favorite,
            background = AppColors.Text_Pink,
            iconTint = Color.White,
            size = 96.dp,
            shadow = 16.dp
        ) { onLike() }
        ActionButton(
            icon = Icons.Filled.Star,
            background = Color(0xFF4A154B),
            iconTint = Color.White,
            size = 72.dp,
            shadow = 10.dp
        ) { onSuperLike() }
    }
}*/
