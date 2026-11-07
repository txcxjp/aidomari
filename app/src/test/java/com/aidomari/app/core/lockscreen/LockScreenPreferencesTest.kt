package com.aidomari.app.core.lockscreen

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
class LockScreenPreferencesTest {

    private lateinit var context: Context
    private lateinit var preferences: LockScreenPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aidomari_lock_screen_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        preferences = LockScreenPreferences(context)
    }

    @Test
    fun defaultValues_areDisabledAndAllTarget() {
        val config = preferences.config.value
        assertFalse(config.isEnabled)
        assertEquals(LockScreenTarget.ALL, config.target)
        assertEquals(3, config.daysThreshold)
    }

    @Test
    fun setEnabled_updatesConfigStateFlow() {
        preferences.setEnabled(true)
        assertTrue(preferences.config.value.isEnabled)

        preferences.setEnabled(false)
        assertFalse(preferences.config.value.isEnabled)
    }

    @Test
    fun setTarget_updatesConfigTarget() {
        preferences.setTarget(LockScreenTarget.BOOKMARK_ONLY)
        assertEquals(LockScreenTarget.BOOKMARK_ONLY, preferences.config.value.target)

        preferences.setTarget(LockScreenTarget.DAYS_AGO)
        assertEquals(LockScreenTarget.DAYS_AGO, preferences.config.value.target)

        preferences.setTarget(LockScreenTarget.ALL)
        assertEquals(LockScreenTarget.ALL, preferences.config.value.target)
    }

    @Test
    fun setDaysThreshold_updatesConfigDaysThreshold() {
        preferences.setDaysThreshold(7)
        assertEquals(7, preferences.config.value.daysThreshold)

        preferences.setDaysThreshold(14)
        assertEquals(14, preferences.config.value.daysThreshold)
    }

    @Test
    fun updateConfig_updatesAllFields() {
        val newConfig = LockScreenConfig(
            isEnabled = true,
            target = LockScreenTarget.DAYS_AGO,
            daysThreshold = 7,
        )
        preferences.updateConfig(newConfig)

        assertEquals(newConfig, preferences.config.value)
    }
}
