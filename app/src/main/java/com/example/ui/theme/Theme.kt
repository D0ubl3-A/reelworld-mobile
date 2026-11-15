package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ReelWorldDarkColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = ObsidianBg,
    primaryContainer = CinemaGoldSoft,
    onPrimaryContainer = CinemaGold,
    secondary = ReelCrimson,
    onSecondary = IvoryText,
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = ElevatedSlate,
    onSurface = IvoryText,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = MutedSlate,
    outline = OutlineSubtle,
)

@Composable
fun ReelWorldTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ReelWorldDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
