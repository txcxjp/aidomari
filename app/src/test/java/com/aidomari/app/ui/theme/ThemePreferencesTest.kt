package com.aidomari.app.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemePreferencesTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aidomari_theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun defaultValues_areSystemAndIndigo() {
        val prefs = ThemePreferences(context)
        val config = prefs.themeConfig.value

        assertEquals(ThemeMode.SYSTEM, config.mode)
        assertEquals(AppColorTheme.INDIGO, config.colorTheme)
    }

    @Test
    fun setThemeMode_updatesStateAndPersists() {
        val prefs = ThemePreferences(context)
        prefs.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, prefs.themeConfig.value.mode)

        // Verify persistence with a new instance
        val reloadedPrefs = ThemePreferences(context)
        assertEquals(ThemeMode.DARK, reloadedPrefs.themeConfig.value.mode)
        assertEquals(AppColorTheme.INDIGO, reloadedPrefs.themeConfig.value.colorTheme)
    }

    @Test
    fun setColorTheme_updatesStateAndPersists() {
        val prefs = ThemePreferences(context)
        prefs.setColorTheme(AppColorTheme.SAKURA)

        assertEquals(AppColorTheme.SAKURA, prefs.themeConfig.value.colorTheme)

        // Verify persistence with a new instance
        val reloadedPrefs = ThemePreferences(context)
        assertEquals(ThemeMode.SYSTEM, reloadedPrefs.themeConfig.value.mode)
        assertEquals(AppColorTheme.SAKURA, reloadedPrefs.themeConfig.value.colorTheme)
    }

    @Test
    fun setThemeConfig_updatesBothModeAndColor() {
        val prefs = ThemePreferences(context)
        prefs.setThemeConfig(ThemeConfig(mode = ThemeMode.LIGHT, colorTheme = AppColorTheme.FOREST))

        assertEquals(ThemeMode.LIGHT, prefs.themeConfig.value.mode)
        assertEquals(AppColorTheme.FOREST, prefs.themeConfig.value.colorTheme)

        val reloadedPrefs = ThemePreferences(context)
        assertEquals(ThemeMode.LIGHT, reloadedPrefs.themeConfig.value.mode)
        assertEquals(AppColorTheme.FOREST, reloadedPrefs.themeConfig.value.colorTheme)
    }
}
