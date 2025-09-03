package com.example.dating.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dating.R
import com.example.dating.data.model.User
import com.example.dating.ui.profile.Interest
import com.example.dating.ui.theme.AppColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileInfoCard(
    user: User,
    images: List<String>,
    onSeeAll: (List<String>) -> Unit,
    onImageClick: (Int, List<String>) -> Unit,
) {
    val firstName = user.firstName.orEmpty()
    val lastName = user.lastName.orEmpty()
    val name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Unknown" }
    val birthday = user.birthday
    val ageText = birthday?.let {
        runCatching {
            val y = it.split("/").getOrNull(2)?.toInt() ?: return@let ""
            val cy = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            " , ${(cy - y)}"
        }.getOrDefault("")
    } ?: ""
    val job = user.job.orEmpty().ifBlank { "Professional model" }
    val location = user.location.orEmpty().ifBlank { "Ho Chi Minh City, Viet Nam" }
    val about = user.description.orEmpty().ifBlank { "My name is $name and I enjoy meeting new people and finding ways to help them have an uplifting experience..." }

    val allInterests = listOf(
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
    ) // :contentReference[oaicite:1]{index=1}
    val selected = user.interests.toSet()
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = AppColors.Main_Secondary1,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .offset(y = (-120).dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Spacer(Modifier.height(50.dp))

            // ===== Header: name + age + ic_location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "$name$ageText",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = job,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ic_location
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = "Location",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFFF8E6F2),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_local),
                            contentDescription = "Location",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "",
                            color = AppColors.Text_Pink,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))


            // ===== About
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = about,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!expanded) {
                Text(
                    "Read more",
                    color = AppColors.Text_Pink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { expanded = true }
                )
            }

            // ===== Interests
            Spacer(Modifier.height(24.dp))
            Text("Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allInterests.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { interest ->
                            val isSelected = selected.contains(interest.name)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AppColors.Text_Pink.copy(alpha = .12f) else Color.White.copy(alpha = .6f),
                                border = if (isSelected) BorderStroke(2.dp, AppColors.Text_Pink) else BorderStroke(1.dp, Color.LightGray.copy(.35f)),
                                tonalElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(interest.icon),
                                        contentDescription = interest.name,
                                        tint = if (isSelected) AppColors.Text_Pink else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        interest.name,
                                        color = if (isSelected) AppColors.Text_Pink else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                        repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // ===== Gallery
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "See all",
                    color = AppColors.Text_Pink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { if (images.isNotEmpty()) onSeeAll(images) }
                )
            }
            Spacer(Modifier.height(8.dp))

            if (images.isNotEmpty()) {
                val preview = images.take(6)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((preview.size + 2) / 3 * 110).dp)
                ) {
                    items(preview.size) { idx ->
                        AsyncImage(
                            model = preview[idx],
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.ic_avatar),
                            error = painterResource(R.drawable.ic_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(idx, images) }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No images available", color = AppColors.Main_Pink)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
