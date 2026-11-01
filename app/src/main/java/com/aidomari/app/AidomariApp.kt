package com.aidomari.app

import android.app.Application
import com.aidomari.app.data.local.AidomariDatabase
import com.aidomari.app.data.repository.WordsRepositoryImpl
import com.aidomari.app.domain.repository.WordsRepository

class AidomariApp : Application() {

    val database: AidomariDatabase by lazy {
        AidomariDatabase.getInstance(this)
    }

    val repository: WordsRepository by lazy {
        WordsRepositoryImpl(database.aidomariDao())
    }

    override fun onCreate() {
        super.onCreate()
    }
}
