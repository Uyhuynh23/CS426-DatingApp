package com.example.dating.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dating.ui.theme.AppColors

@Composable
fun BottomNavIcon(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) AppColors.Text_Pink else Color(0xFFBDBDBD),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, isPageActive: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White.copy(alpha = 1.0f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavIcon(
                icon = Icons.Default.ViewModule,
                isActive = (isPageActive == 0)
            ) { navController.navigate("home") }

            BottomNavIcon(
                icon = Icons.Default.Favorite,
                isActive = (isPageActive == 1)
            ) { navController.navigate("favorite") }

            BottomNavIcon(
                icon = Icons.Default.Chat,
                isActive = (isPageActive == 2)
            ) { navController.navigate("messages") }

            BottomNavIcon(
                icon = Icons.Default.Person,
                isActive = (isPageActive == 3)
            ) { navController.navigate("profile_details") }
        }
    }
}
