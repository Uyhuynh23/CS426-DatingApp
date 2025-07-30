package com.example.dating.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset

val Main_Primary = Color(0xFF4B164C)
val Main_Secondary1 = Color(0xFFFCF3FA)
val Main_Black = Color(0xFF22172A)
val Main_Pink = Color(0xFFB12886)
val Main_PinkBackground = Color(0xFFFDF7FD)


val Text_Pink = Color(0xFFDD88CF)
val Text_Black = Color(0xFF22172A)
val Text_LightBlack = Color(0xFF000000)
val Text_White = Color(0xFFFFFFFF)

val grad1 = Color(0xFFB042DB) //10%
val grad2 = Color(0xFFE940D8) //60%
val grad3 = Color(0xFF8A2387) //100%

val Main_Gradient = Brush.linearGradient(
    colors = listOf(grad1, grad2, grad3),
    start = Offset(0f, 0f),
    end = Offset(0f, 1000f) // hướng từ trên xuống
)
