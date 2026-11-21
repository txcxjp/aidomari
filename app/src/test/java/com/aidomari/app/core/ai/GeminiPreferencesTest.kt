package com.aidomari.app.core.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiPreferencesTest {

    private lateinit var context: Context
    private lateinit var preferences: GeminiPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aidomari_gemini_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        preferences = GeminiPreferences(context)
    }

    @Test
    fun defaultValues_areBlankKeyAndDefaultFlashModel() {
        val config = preferences.config.value
        assertEquals("", config.apiKey)
        assertEquals("gemini-1.5-flash", config.modelName)
        assertEquals(0.2f, config.temperature, 0.001f)
        assertFalse(config.isConfigured)
    }

    @Test
    fun setApiKey_updatesConfigAndIsConfigured() {
        preferences.setApiKey("  AIzaSyMockKey123  ")

        val config = preferences.config.value
        assertEquals("AIzaSyMockKey123", config.apiKey)
        assertTrue(config.isConfigured)
    }

    @Test
    fun updateConfig_persistsCustomModelAndTemp() {
        preferences.updateConfig(
            GeminiConfig(
                apiKey = "mock_key",
                modelName = "gemini-2.0-flash",
                temperature = 0.5f,
            ),
        )

        val config = preferences.config.value
        assertEquals("mock_key", config.apiKey)
        assertEquals("gemini-2.0-flash", config.modelName)
        assertEquals(0.5f, config.temperature, 0.001f)

        // Re-instantiate from prefs
        val reloaded = GeminiPreferences(context)
        assertEquals("mock_key", reloaded.config.value.apiKey)
        assertEquals("gemini-2.0-flash", reloaded.config.value.modelName)
        assertEquals(0.5f, reloaded.config.value.temperature, 0.001f)
    }

    @Test
    fun clearApiKey_resetsApiKeyToEmpty() {
        preferences.setApiKey("AIzaSySecret")
        assertTrue(preferences.config.value.isConfigured)

        preferences.clearApiKey()
        assertEquals("", preferences.config.value.apiKey)
        assertFalse(preferences.config.value.isConfigured)
    }
}
