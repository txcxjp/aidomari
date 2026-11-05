package com.aidomari.app.core.lockscreen

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BootReceiverTest {

    private lateinit var context: Context
    private lateinit var preferences: LockScreenPreferences
    private lateinit var receiver: BootReceiver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aidomari_lock_screen_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        preferences = LockScreenPreferences(context)
        receiver = BootReceiver()
    }

    @Test
    fun onReceive_bootCompleted_whenEnabled_startsService() {
        preferences.setEnabled(true)
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        val shadowApp = shadowOf(context as Application)
        val startedService = shadowApp.nextStartedService
        assertNotNull(startedService)
        assertEquals(LockScreenStudyService::class.java.name, startedService.component?.className)
    }

    @Test
    fun onReceive_myPackageReplaced_whenEnabled_startsService() {
        preferences.setEnabled(true)
        val intent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)

        receiver.onReceive(context, intent)

        val shadowApp = shadowOf(context as Application)
        val startedService = shadowApp.nextStartedService
        assertNotNull(startedService)
        assertEquals(LockScreenStudyService::class.java.name, startedService.component?.className)
    }

    @Test
    fun onReceive_whenDisabled_doesNotStartService() {
        preferences.setEnabled(false)
        val intent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)

        receiver.onReceive(context, intent)

        val shadowApp = shadowOf(context as Application)
        val startedService = shadowApp.nextStartedService
        assertNull(startedService)
    }
}
