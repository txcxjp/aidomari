package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordsRepositoryImplTest {

    private lateinit var db: AidomariDatabase
    private lateinit var repository: WordsRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WordsRepositoryImpl(db.aidomariDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun getSugorokuProgressSync_initializesDefaultProgressIfEmpty() = runBlocking {
        val progress = repository.getSugorokuProgressSync()
        assertEquals(0, progress.currentStepIndex)
        assertEquals(0, progress.currentProgressInStep)
        assertEquals(5, progress.targetQuestionsPerStep)
        assertEquals(setOf("node_000"), progress.visitedNodeIds)

        // Verify it was persisted
        val secondCall = repository.getSugorokuProgressSync()
        assertEquals(progress.id, secondCall.id)
    }

    @Test
    fun recordSugorokuProgress_advancesStepsAndEmitsFlow() = runBlocking {
        // Initial
        val initial = repository.getSugorokuProgress().first()
        assertEquals(0, initial.currentStepIndex)

        // Advance by 3 questions (no node step advance yet, currentProgressInStep = 3)
        val p1 = repository.recordSugorokuProgress(3)
        assertEquals(0, p1.currentStepIndex)
        assertEquals(3, p1.currentProgressInStep)
        assertEquals(3, p1.totalQuestionsAnswered)

        // Advance by 2 more questions (total 5 -> should advance to step 1)
        val p2 = repository.recordSugorokuProgress(2)
        assertEquals(1, p2.currentStepIndex)
        assertEquals(0, p2.currentProgressInStep)
        assertEquals(5, p2.totalQuestionsAnswered)
        assertTrue(p2.visitedNodeIds.contains("node_001"))

        // Flow should reflect updated progress
        val flowValue = repository.getSugorokuProgress().first()
        assertEquals(1, flowValue.currentStepIndex)
        assertEquals(0, flowValue.currentProgressInStep)
        assertEquals(5, flowValue.totalQuestionsAnswered)
    }
}
