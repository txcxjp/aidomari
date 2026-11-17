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
        assertEquals(1, progress.targetQuestionsPerStep)
        assertEquals(setOf("hakodateyama"), progress.visitedNodeIds)

        // Verify it was persisted
        val secondCall = repository.getSugorokuProgressSync()
        assertEquals(progress.id, secondCall.id)
    }

    @Test
    fun recordSugorokuProgress_advancesStepsAndEmitsFlow() = runBlocking {
        // Initial
        val initial = repository.getSugorokuProgress().first()
        assertEquals(0, initial.currentStepIndex)

        // Advance by 1 question (advances to step 1)
        val p1 = repository.recordSugorokuProgress(1)
        assertEquals(1, p1.currentStepIndex)
        assertEquals(0, p1.currentProgressInStep)
        assertEquals(1, p1.totalQuestionsAnswered)
        assertTrue(p1.visitedNodeIds.contains(com.aidomari.app.domain.model.HokkaidoAct1Route.routeNodes[1].id))

        // Advance by 2 more questions (advances to step 3)
        val p2 = repository.recordSugorokuProgress(2)
        assertEquals(3, p2.currentStepIndex)
        assertEquals(0, p2.currentProgressInStep)
        assertEquals(3, p2.totalQuestionsAnswered)
        assertTrue(p2.visitedNodeIds.contains(com.aidomari.app.domain.model.HokkaidoAct1Route.routeNodes[3].id))

        // Flow should reflect updated progress
        val flowValue = repository.getSugorokuProgress().first()
        assertEquals(3, flowValue.currentStepIndex)
        assertEquals(0, flowValue.currentProgressInStep)
        assertEquals(3, flowValue.totalQuestionsAnswered)
    }

    @Test
    fun recordStudyWalkingProgress_advancesMetersAndUpdatesAnchor() = runBlocking {
        // 10 seconds @ 1x => ~13.33m
        val p1 = repository.recordStudyWalkingProgress(durationSeconds = 10f, speedMultiplier = 1f)
        assertEquals(13.333f, p1.totalDistanceMeters, 0.01f)
        assertEquals(0, p1.currentStepIndex)
        assertEquals(1, p1.totalQuestionsAnswered)

        // Advance 550m more to surpass leg 0 (558m)
        val p2 = repository.recordSugorokuDistance(550f)
        assertTrue(p2.totalDistanceMeters >= 558f)
        assertEquals(1, p2.currentStepIndex)
        assertTrue(p2.visitedNodeIds.contains("monument_of_yanagawa_kumakichi"))

        // Reset
        val pReset = repository.resetSugorokuProgress()
        assertEquals(0f, pReset.totalDistanceMeters, 0.001f)
        assertEquals(0, pReset.currentStepIndex)
        assertEquals(setOf("hakodateyama"), pReset.visitedNodeIds)
    }
}
