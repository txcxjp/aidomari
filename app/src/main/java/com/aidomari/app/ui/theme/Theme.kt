package com.aidomari.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeConfig = staticCompositionLocalOf { ThemeConfig() }

fun getLightColorScheme(colorTheme: AppColorTheme): ColorScheme = when (colorTheme) {
    AppColorTheme.INDIGO -> lightColorScheme(
        primary = IndigoPrimaryLight,
        onPrimary = IndigoOnPrimaryLight,
        primaryContainer = IndigoPrimaryContainerLight,
        onPrimaryContainer = IndigoOnPrimaryContainerLight,
        secondary = IndigoSecondaryLight,
        onSecondary = IndigoOnSecondaryLight,
        secondaryContainer = IndigoSecondaryContainerLight,
        onSecondaryContainer = IndigoOnSecondaryContainerLight,
    )
    AppColorTheme.FOREST -> lightColorScheme(
        primary = ForestPrimaryLight,
        onPrimary = ForestOnPrimaryLight,
        primaryContainer = ForestPrimaryContainerLight,
        onPrimaryContainer = ForestOnPrimaryContainerLight,
        secondary = ForestSecondaryLight,
        onSecondary = ForestOnSecondaryLight,
        secondaryContainer = ForestSecondaryContainerLight,
        onSecondaryContainer = ForestOnSecondaryContainerLight,
    )
    AppColorTheme.SAKURA -> lightColorScheme(
        primary = SakuraPrimaryLight,
        onPrimary = SakuraOnPrimaryLight,
        primaryContainer = SakuraPrimaryContainerLight,
        onPrimaryContainer = SakuraOnPrimaryContainerLight,
        secondary = SakuraSecondaryLight,
        onSecondary = SakuraOnSecondaryLight,
        secondaryContainer = SakuraSecondaryContainerLight,
        onSecondaryContainer = SakuraOnSecondaryContainerLight,
    )
    AppColorTheme.AMBER -> lightColorScheme(
        primary = AmberPrimaryLight,
        onPrimary = AmberOnPrimaryLight,
        primaryContainer = AmberPrimaryContainerLight,
        onPrimaryContainer = AmberOnPrimaryContainerLight,
        secondary = AmberSecondaryLight,
        onSecondary = AmberOnSecondaryLight,
        secondaryContainer = AmberSecondaryContainerLight,
        onSecondaryContainer = AmberOnSecondaryContainerLight,
    )
    AppColorTheme.SLATE -> lightColorScheme(
        primary = SlatePrimaryLight,
        onPrimary = SlateOnPrimaryLight,
        primaryContainer = SlatePrimaryContainerLight,
        onPrimaryContainer = SlateOnPrimaryContainerLight,
        secondary = SlateSecondaryLight,
        onSecondary = SlateOnSecondaryLight,
        secondaryContainer = SlateSecondaryContainerLight,
        onSecondaryContainer = SlateOnSecondaryContainerLight,
    )
}

fun getDarkColorScheme(colorTheme: AppColorTheme): ColorScheme = when (colorTheme) {
    AppColorTheme.INDIGO -> darkColorScheme(
        primary = IndigoPrimaryDark,
        onPrimary = IndigoOnPrimaryDark,
        primaryContainer = IndigoPrimaryContainerDark,
        onPrimaryContainer = IndigoOnPrimaryContainerDark,
        secondary = IndigoSecondaryDark,
        onSecondary = IndigoOnSecondaryDark,
        secondaryContainer = IndigoSecondaryContainerDark,
        onSecondaryContainer = IndigoOnSecondaryContainerDark,
    )
    AppColorTheme.FOREST -> darkColorScheme(
        primary = ForestPrimaryDark,
        onPrimary = ForestOnPrimaryDark,
        primaryContainer = ForestPrimaryContainerDark,
        onPrimaryContainer = ForestOnPrimaryContainerDark,
        secondary = ForestSecondaryDark,
        onSecondary = ForestOnSecondaryDark,
        secondaryContainer = ForestSecondaryContainerDark,
        onSecondaryContainer = ForestOnSecondaryContainerDark,
    )
    AppColorTheme.SAKURA -> darkColorScheme(
        primary = SakuraPrimaryDark,
        onPrimary = SakuraOnPrimaryDark,
        primaryContainer = SakuraPrimaryContainerDark,
        onPrimaryContainer = SakuraOnPrimaryContainerDark,
        secondary = SakuraSecondaryDark,
        onSecondary = SakuraOnSecondaryDark,
        secondaryContainer = SakuraSecondaryContainerDark,
        onSecondaryContainer = SakuraOnSecondaryContainerDark,
    )
    AppColorTheme.AMBER -> darkColorScheme(
        primary = AmberPrimaryDark,
        onPrimary = AmberOnPrimaryDark,
        primaryContainer = AmberPrimaryContainerDark,
        onPrimaryContainer = AmberOnPrimaryContainerDark,
        secondary = AmberSecondaryDark,
        onSecondary = AmberOnSecondaryDark,
        secondaryContainer = AmberSecondaryContainerDark,
        onSecondaryContainer = AmberOnSecondaryContainerDark,
    )
    AppColorTheme.SLATE -> darkColorScheme(
        primary = SlatePrimaryDark,
        onPrimary = SlateOnPrimaryDark,
        primaryContainer = SlatePrimaryContainerDark,
        onPrimaryContainer = SlateOnPrimaryContainerDark,
        secondary = SlateSecondaryDark,
        onSecondary = SlateOnSecondaryDark,
        secondaryContainer = SlateSecondaryContainerDark,
        onSecondaryContainer = SlateOnSecondaryContainerDark,
    )
}

@Composable
fun AidomariTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit,
) {
    val isDark = when (themeConfig.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        getDarkColorScheme(themeConfig.colorTheme)
    } else {
        getLightColorScheme(themeConfig.colorTheme)
    }

    CompositionLocalProvider(LocalThemeConfig provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
