package com.aidomari.app

import android.app.Application
import com.aidomari.app.core.database.AidomariDatabase
import com.aidomari.app.core.database.WordsRepositoryImpl
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.ui.theme.ThemePreferences

class AidomariApp : Application() {

    val database: AidomariDatabase by lazy {
        AidomariDatabase.getInstance(this)
    }

    val repository: WordsRepository by lazy {
        WordsRepositoryImpl(database.aidomariDao())
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this)
    }

    val lockScreenPreferences: LockScreenPreferences by lazy {
        LockScreenPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
