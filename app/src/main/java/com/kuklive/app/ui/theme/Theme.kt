package com.kuklive.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Brand = Color(0xFF7C5CFF)
val BrandSecondary = Color(0xFFFF5C8A)
val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF15151F)

private val DarkColors = darkColorScheme(
    primary = Brand,
    secondary = BrandSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = Color(0xFFF5F5F7),
    onSurface = Color(0xFFF5F5F7),
)

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = BrandSecondary,
)

@Composable
fun KukliveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
