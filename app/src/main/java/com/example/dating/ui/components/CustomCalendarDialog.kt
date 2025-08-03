package com.example.dating.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*
import com.example.dating.ui.theme.AppColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomCalendarDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var visibleMonth by remember { mutableStateOf(currentDate.timeInMillis) }
    var selectedDate by remember { mutableStateOf<Int?>(null) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calendar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) {
                        Text("Skip", color = AppColors.Text_Pink, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Birthday",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Year & Month with arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        animationDirection = -1
                        currentDate.add(Calendar.MONTH, -1)
                        currentDate = currentDate.clone() as Calendar
                        visibleMonth = currentDate.timeInMillis
                        selectedDate = null
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${currentDate.get(Calendar.YEAR)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Text_Pink,
                            modifier = Modifier.clickable {
                                showYearPicker = !showYearPicker
                                showMonthPicker = false
                            }
                        )
                        Text(
                            SimpleDateFormat("MMMM", Locale.getDefault()).format(currentDate.time),
                            fontSize = 16.sp,
                            color = AppColors.Text_Pink,
                            modifier = Modifier.clickable {
                                showMonthPicker = !showMonthPicker
                                showYearPicker = false
                            }
                        )
                    }

                    IconButton(onClick = {
                        animationDirection = 1
                        currentDate.add(Calendar.MONTH, 1)
                        currentDate = currentDate.clone() as Calendar
                        visibleMonth = currentDate.timeInMillis
                        selectedDate = null
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Content
                Box(Modifier.height(260.dp)) {
                    when {
                        showYearPicker -> YearPicker(
                            currentYear = currentDate.get(Calendar.YEAR),
                            onYearSelected = { year ->
                                currentDate.set(Calendar.YEAR, year)
                                currentDate = currentDate.clone() as Calendar
                                visibleMonth = currentDate.timeInMillis
                                showYearPicker = false
                                selectedDate = null
                            }
                        )
                        showMonthPicker -> MonthPicker(
                            currentMonth = currentDate.get(Calendar.MONTH),
                            onMonthSelected = { month ->
                                currentDate.set(Calendar.MONTH, month)
                                currentDate = currentDate.clone() as Calendar
                                visibleMonth = currentDate.timeInMillis
                                showMonthPicker = false
                                selectedDate = null
                            }
                        )
                        else -> AnimatedContent(
                            targetState = visibleMonth,
                            transitionSpec = {
                                if (animationDirection >= 0) {
                                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() with
                                            fadeOut(animationSpec = tween(150))
                                } else {
                                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn() with
                                            fadeOut(animationSpec = tween(150))
                                }
                            },
                            label = "CalendarSlide"
                        ) { targetTime ->
                            val monthCalendar = Calendar.getInstance().apply { timeInMillis = targetTime }
                            CalendarGrid(
                                currentDate = monthCalendar,
                                selectedDate = selectedDate,
                                onDateSelected = { selectedDate = it }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedDate?.let { day ->
                            val calendar = currentDate.clone() as Calendar
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            onDateSelected(calendar.time)
                        }
                    },
                    enabled = selectedDate != null && !showYearPicker && !showMonthPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Main_Secondary1,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text("Save", color = AppColors.Main_Primary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentDate: Calendar,
    selectedDate: Int?,
    onDateSelected: (Int) -> Unit
) {
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Luôn 42 ô để mượt
    val totalCells = 42
    val days = (0 until totalCells).map { index ->
        val dayNumber = index - firstDayOfWeek + 1
        if (dayNumber in 1..daysInMonth) dayNumber else null
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(264.dp),
        userScrollEnabled = false
    ) {
        items(days.size) { index ->
            val day = days[index]
            if (day == null) {
                Box(modifier = Modifier.size(40.dp))
            } else {
                CalendarDay(
                    day = day,
                    isSelected = day == selectedDate,
                    onClick = { onDateSelected(day) }
                )
            }
        }
    }
}

@Composable
fun CalendarDay(day: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) AppColors.Main_Secondary1 else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 16.sp,
            color = if (isSelected) AppColors.Main_Primary else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun YearPicker(currentYear: Int, onYearSelected: (Int) -> Unit) {
    val years = (1950..2010).toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(years) { year ->
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (year == currentYear) AppColors.Main_Secondary1 else Color(0xFFF5F5F5))
                    .clickable { onYearSelected(year) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = year.toString(),
                    fontSize = 14.sp,
                    color = if (year == currentYear) AppColors.Main_Primary else Color.Black,
                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun MonthPicker(currentMonth: Int, onMonthSelected: (Int) -> Unit) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(months.size) { index ->
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (index == currentMonth) AppColors.Main_Secondary1 else Color(0xFFF5F5F5))
                    .clickable { onMonthSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = months[index].take(3),
                    fontSize = 14.sp,
                    color = if (index == currentMonth) AppColors.Main_Primary else Color.Black,
                    fontWeight = if (index == currentMonth) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}