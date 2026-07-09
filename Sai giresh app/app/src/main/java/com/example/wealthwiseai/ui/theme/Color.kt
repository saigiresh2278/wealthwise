package com.example.wealthwiseai.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Premium Fintech Palette (Dark Mode First)
val DarkBg = Color(0xFF0A0F1D)        // Deep slate black
val GlassWhite = Color(0x14FFFFFF)     // 8% opacity white for glassmorphism
val BlueAccent = Color(0xFF3F8CFF)     // Vibrant electric blue
val GreenAccent = Color(0xFF00E676)    // Glowing green for growth
val GoldAccent = Color(0xFFFFB300)     // Warm amber for goals and risk
val RedAccent = Color(0xFFFF3D00)      // Hot orange-red for leaks & expenses
val BorderWhite = Color(0x2BFFFFFF)    // 17% white border

val CardBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val TextWhite: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

val TextGray: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
