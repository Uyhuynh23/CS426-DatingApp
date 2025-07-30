package com.example.dating.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomCalendarDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Int?>(null) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    // Animation states
    var isAnimating by remember { mutableStateOf(false) }
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
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calendar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Skip",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Birthday label
                Text(
                    text = "Birthday",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Year and Month navigation - FIXED VERSION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Arrow
                    IconButton(
                        onClick = {
                            if (!isAnimating) {
                                animationDirection = -1
                                isAnimating = true
                            }
                        },
                        enabled = !isAnimating
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = if (isAnimating) Color.Gray else Color.Black
                        )
                    }

                    // Year and Month Display - Update ngay lập tức
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Clickable Year
                        Text(
                            text = "${currentDate.get(Calendar.YEAR)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63),
                            modifier = Modifier.clickable {
                                showYearPicker = !showYearPicker
                                showMonthPicker = false
                            }
                        )

                        // Clickable Month
                        Text(
                            text = SimpleDateFormat("MMMM", Locale.getDefault()).format(currentDate.time),
                            fontSize = 16.sp,
                            color = Color(0xFFE91E63),
                            modifier = Modifier.clickable {
                                showMonthPicker = !showMonthPicker
                                showYearPicker = false
                            }
                        )
                    }

                    // Right Arrow
                    IconButton(
                        onClick = {
                            if (!isAnimating) {
                                animationDirection = 1
                                isAnimating = true
                            }
                        },
                        enabled = !isAnimating
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = if (isAnimating) Color.Gray else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content with animation
                Box(
                    modifier = Modifier.height(200.dp)
                ) {
                    when {
                        showYearPicker -> {
                            YearPicker(
                                currentYear = currentDate.get(Calendar.YEAR),
                                onYearSelected = { year ->
                                    currentDate.set(Calendar.YEAR, year)
                                    // Force recomposition by cloning
                                    currentDate = currentDate.clone() as Calendar
                                    showYearPicker = false
                                    selectedDate = null
                                }
                            )
                        }
                        showMonthPicker -> {
                            MonthPicker(
                                currentMonth = currentDate.get(Calendar.MONTH),
                                onMonthSelected = { month ->
                                    currentDate.set(Calendar.MONTH, month)
                                    // Force recomposition by cloning
                                    currentDate = currentDate.clone() as Calendar
                                    showMonthPicker = false
                                    selectedDate = null
                                }
                            )
                        }
                        else -> {
                            AnimatedCalendarGrid(
                                currentDate = currentDate,
                                selectedDate = selectedDate,
                                isAnimating = isAnimating,
                                animationDirection = animationDirection,
                                onDateSelected = { day ->
                                    selectedDate = day
                                },
                                onAnimationComplete = {
                                    // Update the date after animation
                                    if (animationDirection == -1) {
                                        currentDate.add(Calendar.MONTH, -1)
                                    } else {
                                        currentDate.add(Calendar.MONTH, 1)
                                    }
                                    currentDate = currentDate.clone() as Calendar
                                    selectedDate = null
                                    isAnimating = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        selectedDate?.let { day ->
                            val calendar = currentDate.clone() as Calendar
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            onDateSelected(calendar.time)
                        }
                    },
                    enabled = selectedDate != null && !showYearPicker && !showMonthPicker && !isAnimating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Các component khác giữ nguyên...
@Composable
fun AnimatedCalendarGrid(
    currentDate: Calendar,
    selectedDate: Int?,
    isAnimating: Boolean,
    animationDirection: Int,
    onDateSelected: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { 300.dp.toPx() }.toInt()

    val offsetX by animateIntAsState(
        targetValue = if (isAnimating) screenWidth * -animationDirection else 0,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            if (isAnimating && it != 0) {
                onAnimationComplete()
            }
        },
        label = "CalendarSlideAnimation"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX, 0) }
        ) {
            CalendarGrid(
                currentDate = currentDate,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                enabled = !isAnimating
            )
        }

        if (isAnimating) {
            val previewDate = currentDate.clone() as Calendar
            if (animationDirection == 1) {
                previewDate.add(Calendar.MONTH, 1)
            } else {
                previewDate.add(Calendar.MONTH, -1)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX + screenWidth * animationDirection, 0) }
            ) {
                CalendarGrid(
                    currentDate = previewDate,
                    selectedDate = null,
                    onDateSelected = { },
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentDate: Calendar,
    selectedDate: Int?,
    onDateSelected: (Int) -> Unit,
    enabled: Boolean = true
) {
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = enabled
    ) {
        items(firstDayOfWeek) {
            Box(modifier = Modifier.size(40.dp))
        }

        items((1..daysInMonth).toList()) { day ->
            CalendarDay(
                day = day,
                isSelected = day == selectedDate,
                enabled = enabled,
                onClick = { if (enabled) onDateSelected(day) }
            )
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFFE91E63) else Color.Transparent
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 16.sp,
            color = when {
                isSelected -> Color.White
                enabled -> Color.Black
                else -> Color.Gray
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun YearPicker(
    currentYear: Int,
    onYearSelected: (Int) -> Unit
) {
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
                    .background(
                        if (year == currentYear) Color(0xFFE91E63) else Color(0xFFF5F5F5)
                    )
                    .clickable { onYearSelected(year) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = year.toString(),
                    fontSize = 14.sp,
                    color = if (year == currentYear) Color.White else Color.Black,
                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun MonthPicker(
    currentMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
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
                    .background(
                        if (index == currentMonth) Color(0xFFE91E63) else Color(0xFFF5F5F5)
                    )
                    .clickable { onMonthSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = months[index].take(3),
                    fontSize = 14.sp,
                    color = if (index == currentMonth) Color.White else Color.Black,
                    fontWeight = if (index == currentMonth) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
