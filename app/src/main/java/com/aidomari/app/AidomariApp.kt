package com.aidomari.app

import android.app.Application
import com.aidomari.app.core.ai.AndroidCardGenerationNotificationController
import com.aidomari.app.core.ai.GeminiApiClientImpl
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.core.database.AidomariDatabase
import com.aidomari.app.core.database.SourceArticleRepositoryImpl
import com.aidomari.app.core.database.WordsRepositoryImpl
import com.aidomari.app.core.debug.SugorokuDebugPreferences
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.core.server.LocalIngestServerManager
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.ui.digest.BrowsingDigestViewModel
import com.aidomari.app.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AidomariApp : Application() {

    val database: AidomariDatabase by lazy {
        AidomariDatabase.getInstance(this)
    }

    val repository: WordsRepository by lazy {
        WordsRepositoryImpl(database.aidomariDao(), this)
    }

    val sourceArticleRepository: SourceArticleRepository by lazy {
        SourceArticleRepositoryImpl(database.sourceArticleDao())
    }

    val localIngestServerManager: LocalIngestServerManager by lazy {
        LocalIngestServerManager(sourceArticleRepository)
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this)
    }

    val lockScreenPreferences: LockScreenPreferences by lazy {
        LockScreenPreferences(this)
    }

    val sugorokuDebugPreferences: SugorokuDebugPreferences by lazy {
        SugorokuDebugPreferences(this)
    }

    val browsingDigestViewModel: BrowsingDigestViewModel by lazy {
        BrowsingDigestViewModel(
            sourceArticleRepository = sourceArticleRepository,
            wordsRepository = repository,
            geminiApiClient = GeminiApiClientImpl(),
            geminiPreferences = GeminiPreferences(this),
            notificationController = AndroidCardGenerationNotificationController(this),
        )
    }

    override fun onCreate() {
        super.onCreate()
        localIngestServerManager.startServer()
        if (android.os.Build.FINGERPRINT != "robolectric") {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { sourceArticleRepository.cleanupOldArticles() }
            }
        }
    }
}
