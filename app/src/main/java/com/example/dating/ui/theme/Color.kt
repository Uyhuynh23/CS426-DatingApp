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

val Circle_grad1 = Color(0xFFB042DB) //10%
val Circle_grad2 = Color(0xFFE940D8) //60%
val Circle_grad3 = Color(0xFF8A2387) //100%

val GradientBackground1 = Color(0xFFDFBFFF) //0%
val GradientBackground2 = Color(0xFFFDD2FF) //50%
val GradientBackground3 = Color(0xFFF1DFF1) //100%

val MainBackground11 = Color(0xFFFFFFFF) //0%
val MainBackground12 = Color(0xFFEDD0ED) //50%

val MainBackground21 = Color(0xFFFFE7FB) // 0%
val MainBackground22 = Color(0xFFF9C5F1) // 50%
val MainBackground23 = Color(0xFFFFFFFF) // 100%

val Circle_Gradient = Brush.linearGradient(
    colors = listOf(Circle_grad1, Circle_grad2, Circle_grad3),
    start = Offset(0f, 0f),
    end = Offset(0f, 1000f) // hướng từ trên xuống
)

val GradientBackground = Brush.linearGradient(
    colors = listOf(GradientBackground1, GradientBackground2, GradientBackground3),
    start = Offset(0f, 0f),
    end = Offset(0f, 1000f) // hướng từ trên xuống
)

val MainBackground = Brush.linearGradient(
    colors = listOf(MainBackground11, MainBackground12),
    start = Offset(0f, 0f),
    end = Offset(0f, 1000f) // hướng từ trên xuống
)

val MainBackground2 = Brush.linearGradient(
    colors = listOf(MainBackground21, MainBackground22, MainBackground23),
    start = Offset(0f, 0f),
    end = Offset(0f, 1000f) // hướng từ trên xuống
)
