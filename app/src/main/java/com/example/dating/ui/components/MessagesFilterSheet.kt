package com.example.dating.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.dating.data.model.MessagesFilterState
import com.example.dating.data.model.MsgSort

@Composable
fun MessagesFilterSheet(
    state: MessagesFilterState,
    onChange: (MessagesFilterState) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    val accent = Color(0xFFE55FD1)
    val outline = Color(0xFFE7E7EA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClear) { Text("Clear", color = accent) }
        }

        Spacer(Modifier.height(6.dp))

        FilterSwitchRow(
            title = "Show unread only",
            checked = state.unreadOnly,
            onCheckedChange = { onChange(state.copy(unreadOnly = it)) }
        )
        FilterSwitchRow(
            title = "Online only",
            checked = state.onlineOnly,
            onCheckedChange = { onChange(state.copy(onlineOnly = it)) }
        )

        Spacer(Modifier.height(12.dp))

        Text("Sort by", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortChip("Newest", selected = state.sort == MsgSort.NEWEST, accent) {
                onChange(state.copy(sort = MsgSort.NEWEST))
            }
            SortChip("Oldest", selected = state.sort == MsgSort.OLDEST, accent) {
                onChange(state.copy(sort = MsgSort.OLDEST))
            }
            SortChip("Unread first", selected = state.sort == MsgSort.UNREAD_FIRST, accent) {
                onChange(state.copy(sort = MsgSort.UNREAD_FIRST))
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFF0FA),
                contentColor = Color(0xFF3A0D45)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, outline)
        ) { Text("Continue", fontWeight = FontWeight.SemiBold) }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun FilterSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = accent.copy(alpha = 0.18f),
            labelColor = Color(0xFF1C1C1E),
            selectedLabelColor = Color(0xFF1C1C1E)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) accent else Color(0xFFE7E7EA),
            selectedBorderColor = accent
        )
    )
}
