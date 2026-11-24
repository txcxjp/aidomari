package com.aidomari.app.core.ai

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aidomari.app.MainActivity

interface CardGenerationNotificationController {
    fun onGenerationStarted(articleTitle: String)
    fun onGenerationProgress(articleTitle: String, message: String, fraction: Float)
    fun onGenerationCompleted(articleTitle: String, cardCount: Int)
    fun onGenerationFailed(articleTitle: String, errorMessage: String)
    fun clearCompletionNotification()
}

class AndroidCardGenerationNotificationController(
    private val context: Context,
) : CardGenerationNotificationController {

    private val appContext = context.applicationContext

    override fun onGenerationStarted(articleTitle: String) {
        clearCompletionNotification()
        CardGenerationService.startOrUpdate(
            context = appContext,
            title = formatProgressTitle(articleTitle),
            message = "作成準備中...",
            progressPercent = 5,
        )
    }

    override fun onGenerationProgress(articleTitle: String, message: String, fraction: Float) {
        val percent = (fraction * 100).toInt().coerceIn(0, 100)
        CardGenerationService.startOrUpdate(
            context = appContext,
            title = formatProgressTitle(articleTitle),
            message = "作成中... ($percent%)",
            progressPercent = percent,
        )
    }

    override fun onGenerationCompleted(articleTitle: String, cardCount: Int) {
        CardGenerationService.stop(appContext)
        val displayTitle = articleTitle.ifBlank { "Web記事" }.take(24)
        postResultNotification(
            title = "カードの作成が完了しました（${cardCount}枚）",
            text = "「$displayTitle」— タップして確認・デッキ登録",
        )
    }

    override fun onGenerationFailed(articleTitle: String, errorMessage: String) {
        CardGenerationService.stop(appContext)
        val displayTitle = articleTitle.ifBlank { "Web記事" }.take(20)
        postResultNotification(
            title = "カード作成が中断されました",
            text = "「$displayTitle」: $errorMessage",
        )
    }

    override fun clearCompletionNotification() {
        runCatching {
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(CardGenerationService.RESULT_NOTIFICATION_ID)
        }
    }

    private fun formatProgressTitle(articleTitle: String): String {
        val clean = articleTitle.ifBlank { "Web記事" }.take(20)
        return "カード作成中: $clean"
    }

    private fun postResultNotification(title: String, text: String) {
        if (!CardGenerationService.canPostNotifications(appContext)) return
        runCatching {
            CardGenerationService.ensureChannel(appContext)
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val pendingIntent = CardGenerationService.createDigestPendingIntent(appContext)
            val notification = NotificationCompat.Builder(appContext, CardGenerationService.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            nm.notify(CardGenerationService.RESULT_NOTIFICATION_ID, notification)
        }
    }
}

class CardGenerationService : Service() {

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "カード作成中..."
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "処理中..."
        val progress = intent?.getIntExtra(EXTRA_PROGRESS, 0) ?: 0

        val notification = buildProgressNotification(
            context = this,
            title = title,
            message = message,
            progressPercent = progress,
        )

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    PROGRESS_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(PROGRESS_NOTIFICATION_ID, notification)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "aidomari_card_generation_channel"
        const val PROGRESS_NOTIFICATION_ID = 2101
        const val RESULT_NOTIFICATION_ID = 2102

        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_MESSAGE = "extra_message"
        private const val EXTRA_PROGRESS = "extra_progress"

        fun canPostNotifications(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "カード作成の進捗",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Web記事からのカード作成中に進捗と完了を通知します"
                }
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.createNotificationChannel(channel)
            }
        }

        fun createDigestPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "browsing_digest")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                PROGRESS_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun buildProgressNotification(
            context: Context,
            title: String,
            message: String,
            progressPercent: Int,
        ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(createDigestPendingIntent(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progressPercent.coerceIn(0, 100), false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        fun startOrUpdate(
            context: Context,
            title: String,
            message: String,
            progressPercent: Int,
        ) {
            if (Build.FINGERPRINT == "robolectric") return
            runCatching {
                ensureChannel(context)
                val intent = Intent(context, CardGenerationService::class.java).apply {
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_MESSAGE, message)
                    putExtra(EXTRA_PROGRESS, progressPercent)
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            if (Build.FINGERPRINT == "robolectric") return
            runCatching {
                context.stopService(Intent(context, CardGenerationService::class.java))
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.cancel(PROGRESS_NOTIFICATION_ID)
            }
        }
    }
}
