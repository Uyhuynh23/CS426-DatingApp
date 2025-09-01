package com.example.dating.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.dating.data.model.Interest
import com.example.dating.ui.theme.AppColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

@Composable
fun InterestsSection(
    allInterests: List<Interest>,
    selectedInterests: SnapshotStateList<String>,
    isEditMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        allInterests.chunked(2).forEach { rowInterests ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                rowInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest.name)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) AppColors.Text_Pink.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                        border = if (isSelected) BorderStroke(2.dp, AppColors.Text_Pink) else null,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                            .height(48.dp)
                            .clickable(enabled = isEditMode) {
                                if (isEditMode) {
                                    if (isSelected) selectedInterests.remove(interest.name)
                                    else selectedInterests.add(interest.name)
                                }
                            },
                        tonalElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = interest.icon),
                                contentDescription = interest.name,
                                tint = if (isSelected) AppColors.Text_Pink else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = interest.name,
                                color = if (isSelected) AppColors.Text_Pink else Color.DarkGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                if (rowInterests.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}