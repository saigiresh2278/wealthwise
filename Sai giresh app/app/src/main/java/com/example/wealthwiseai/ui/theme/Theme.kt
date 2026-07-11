package com.example.wealthwiseai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeStyle(val displayName: String) {
    CLASSIC_BLUE("Classic Blue"),
    EMERALD_GREEN("Emerald Green"),
    ROYAL_PURPLE("Royal Purple"),
    GOLDEN_LUXURY("Golden Luxury"),
    CRIMSON_WARM("Crimson Warm")
}

@Composable
fun WealthWiseAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeStyle: ThemeStyle = ThemeStyle.CLASSIC_BLUE,
    content: @Composable () -> Unit
) {
    val primaryColor = when (themeStyle) {
        ThemeStyle.CLASSIC_BLUE -> Color(0xFF3F8CFF)
        ThemeStyle.EMERALD_GREEN -> Color(0xFF00C853)
        ThemeStyle.ROYAL_PURPLE -> Color(0xFF8B5CF6)
        ThemeStyle.GOLDEN_LUXURY -> Color(0xFFFFB300)
        ThemeStyle.CRIMSON_WARM -> Color(0xFFEF4444)
    }

    val secondaryColor = when (themeStyle) {
        ThemeStyle.CLASSIC_BLUE -> Color(0xFF00E676)
        ThemeStyle.EMERALD_GREEN -> Color(0xFF3F8CFF)
        ThemeStyle.ROYAL_PURPLE -> Color(0xFFEC4899)
        ThemeStyle.GOLDEN_LUXURY -> Color(0xFF3F8CFF)
        ThemeStyle.CRIMSON_WARM -> Color(0xFFF97316)
    }

    val tertiaryColor = when (themeStyle) {
        ThemeStyle.CLASSIC_BLUE -> Color(0xFFFFB300)
        ThemeStyle.EMERALD_GREEN -> Color(0xFFFFB300)
        ThemeStyle.ROYAL_PURPLE -> Color(0xFFFFB300)
        ThemeStyle.GOLDEN_LUXURY -> Color(0xFF8B5CF6)
        ThemeStyle.CRIMSON_WARM -> Color(0xFFFFB300)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = DarkBg,
            surface = Color(0xFF151D30),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color(0xFFF3F4F6),
            onSurface = Color(0xFFF3F4F6),
            error = RedAccent
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = Color(0xFFF9FAFB),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.Black,
            onBackground = Color(0xFF111827),
            onSurface = Color(0xFF1F2937),
            error = Color(0xFFDC2626)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

