package com.aidomari.app.core.lockscreen

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aidomari.app.AidomariApp
import com.aidomari.app.MainActivity
import com.aidomari.app.ui.lockscreen.LockScreenStudyActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockScreenStudyService : Service() {

    private var screenReceiver: BroadcastReceiver? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startAsForeground()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterScreenReceiver()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startAsForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("相泊 ロック画面学習")
            .setContentText("画面点灯時に1問演習カードを表示します")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun registerScreenReceiver() {
        if (screenReceiver != null) return

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_ON && context != null) {
                    val keyguardManager =
                        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (keyguardManager?.isKeyguardLocked == true) {
                        val app = context.applicationContext as? AidomariApp
                        val repo = app?.repository
                        val prefs = app?.lockScreenPreferences
                        if (repo != null && prefs != null) {
                            serviceScope.launch(Dispatchers.IO) {
                                val config = prefs.config.value
                                val card = when (config.target) {
                                    LockScreenTarget.BOOKMARK_ONLY -> repo.getRandomCard(bookmarkOnly = true)
                                    LockScreenTarget.DAYS_AGO -> {
                                        val cutoffTimestamp = System.currentTimeMillis() -
                                            (config.daysThreshold.toLong() * MILLIS_PER_DAY)
                                        repo.getRandomCardOverDaysAgo(cutoffTimestamp)
                                            ?: repo.getOldestStudiedCard()
                                    }
                                    LockScreenTarget.ALL -> repo.getRandomCard(bookmarkOnly = false)
                                }
                                if (card != null) {
                                    withContext(Dispatchers.Main) {
                                        val activityIntent =
                                            Intent(context, LockScreenStudyActivity::class.java).apply {
                                                addFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                                                )
                                            }
                                        context.startActivity(activityIntent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        ContextCompat.registerReceiver(
            this,
            screenReceiver!!,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {
            }
            screenReceiver = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ロック画面学習",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "ロック画面での単語学習サービス"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "lock_screen_study_channel"
        const val NOTIFICATION_ID = 2001
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        fun start(context: Context) {
            val intent = Intent(context, LockScreenStudyService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LockScreenStudyService::class.java)
            context.stopService(intent)
        }
    }
}
