package com.example.dating.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun PhotoViewerScreen(
    navController: NavController,
    images: List<String>,
    startIndex: Int
) {
    var index by remember { mutableStateOf(startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top app bar (back)
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        // Main photo
        if (images.isNotEmpty()) {
            AsyncImage(
                model = images[index],
                contentDescription = "Photo $index",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .aspectRatio(3f / 4f) // tỉ lệ gần giống Figma
            )
        }

        // Thumbnails
        if (images.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                itemsIndexed(images) { i, thumb ->
                    AsyncImage(
                        model = thumb,
                        contentDescription = "Thumb $i",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(if (i == index) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) else Modifier)
                            .alpha(if (i == index) 1f else 0.6f)
                            .clickable { index = i }
                    )
                }
            }
        }
    }
}
