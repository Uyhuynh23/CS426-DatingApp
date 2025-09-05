package com.example.dating.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dating.R
import com.example.dating.data.model.User


@Composable
fun ProfileHero(profile: User?, navController: NavController) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        val hero = profile?.avatarUrl ?: profile?.imageUrl?.firstOrNull()
        if (!hero.isNullOrBlank()) {
            AsyncImage(
                model = hero,
                contentDescription = "hero",
                placeholder = painterResource(R.drawable.ic_avatar),
                error = painterResource(R.drawable.ic_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_avatar),
                contentDescription = "hero-placeholder",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
