package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.AppTranslations

val LocalThemeIsDark = compositionLocalOf { false }
val LocalToggleTheme = compositionLocalOf<() -> Unit> { {} }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
val LocalAppStrings = compositionLocalOf { AppTranslations.getStrings(AppLanguage.ENGLISH) }

private val DarkColorScheme = darkColorScheme(
    primary = SocietyTealLight,
    onPrimary = Slate900,
    primaryContainer = SocietyTealDark,
    onPrimaryContainer = SocietyTealContainer,
    secondary = Color(0xFF90CAF9),
    onSecondary = Slate900,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Slate900,
    tertiaryContainer = Color(0xFF4E2C00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = SocietyTealPrimary,
    onPrimary = Color.White,
    primaryContainer = SocietyTealContainer,
    onPrimaryContainer = SocietyOnTealContainer,
    secondary = SapphireNavy,
    onSecondary = Color.White,
    secondaryContainer = SapphireNavyContainer,
    onSecondaryContainer = SapphireNavy,
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300
)

@Composable
fun SocietyManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeIsDark provides darkTheme,
        LocalToggleTheme provides onToggleTheme
    ) {
        MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Accessible, animated Material 3 theme toggle button for switching between
 * light mode and eye-safe dark mode during late-night ledger entries.
 */
@Composable
fun ThemeToggleIconButton(
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalThemeIsDark.current,
    onToggle: () -> Unit = LocalToggleTheme.current,
    testTag: String = "theme_toggle_button"
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDark) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "theme_icon_rotation"
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier
            .testTag(testTag)
            .size(40.dp)
            .background(
                color = if (isDark) Color(0xFF334155) else SapphireNavy.copy(alpha = 0.08f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode for late-night ledger comfort",
            tint = if (isDark) Color(0xFFFFD54F) else SapphireNavy,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(rotationZ = rotation)
        )
    }
}
