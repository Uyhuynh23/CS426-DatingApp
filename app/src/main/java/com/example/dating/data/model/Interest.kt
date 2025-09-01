package com.example.dating.data.model

import com.example.dating.R

data class Interest(
    val name: String,
    val icon: Int
)

val ALL_INTERESTS = listOf(
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
)
