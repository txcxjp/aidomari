package com.aidomari.app.domain.model

import com.aidomari.app.core.database.entity.SugorokuProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SugorokuProgressTest {

    @Test
    fun defaultProgress_initializesAtStartNode() {
        val progress = SugorokuProgress()
        assertEquals("default_progress", progress.id)
        assertEquals(0, progress.currentStepIndex)
        assertEquals(0, progress.currentProgressInStep)
        assertEquals(1, progress.targetQuestionsPerStep)
        assertEquals(0, progress.totalQuestionsAnswered)
        assertEquals(setOf("hakodate_station"), progress.visitedNodeIds)
        assertEquals(0f, progress.progressRatioInStep, 0.001f)
    }

    @Test
    fun progressRatioInStep_calculatesFractionCorrectly() {
        val p1 = SugorokuProgress(currentProgressInStep = 2, targetQuestionsPerStep = 5)
        assertEquals(0.4f, p1.progressRatioInStep, 0.001f)

        val p2 = SugorokuProgress(currentProgressInStep = 5, targetQuestionsPerStep = 5)
        assertEquals(1.0f, p2.progressRatioInStep, 0.001f)

        val p3 = SugorokuProgress(currentProgressInStep = 0, targetQuestionsPerStep = 0)
        assertEquals(0f, p3.progressRatioInStep, 0.001f)
    }

    @Test
    fun advanceByQuestions_zeroOrNegative_doesNotChangeState() {
        val initial = SugorokuProgress()
        val p0 = initial.advanceByQuestions(0)
        assertEquals(initial, p0)

        val pNegative = initial.advanceByQuestions(-3)
        assertEquals(initial, pNegative)
    }

    @Test
    fun advanceByQuestions_oneQuestionAdvancesOneStepByDefault() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 1)
        val advanced = initial.advanceByQuestions(1)

        assertEquals(1, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(1, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("hakodate_station"))
        assertTrue(advanced.visitedNodeIds.contains(HokkaidoAct1Route.routeNodes[1].id))
    }

    @Test
    fun advanceByQuestions_subStepIncrementsProgressOnlyWhenTargetGreaterThanOne() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 5)
        val advanced = initial.advanceByQuestions(3)

        assertEquals(0, advanced.currentStepIndex)
        assertEquals(3, advanced.currentProgressInStep)
        assertEquals(3, advanced.totalQuestionsAnswered)
        assertEquals(setOf("hakodate_station"), advanced.visitedNodeIds)
    }

    @Test
    fun advanceByQuestions_multiStep_advancesMultipleAndCollectsAllVisitedNodes() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 1)
        val advanced = initial.advanceByQuestions(3) // 3 steps: 0 -> 1 -> 2 -> 3

        assertEquals(3, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(3, advanced.totalQuestionsAnswered)
        for (i in 0..3) {
            assertTrue(advanced.visitedNodeIds.contains(HokkaidoAct1Route.routeNodes[i].id))
        }
        assertFalse(advanced.visitedNodeIds.contains(HokkaidoAct1Route.routeNodes[4].id))
    }

    @Test
    fun advanceByQuestions_capsAtGoal() {
        val maxStep = HokkaidoAct1Route.maxStepIndex
        val initial = SugorokuProgress(
            currentStepIndex = maxStep - 5,
            currentProgressInStep = 0,
            targetQuestionsPerStep = 1,
        )
        val advanced = initial.advanceByQuestions(100)

        assertEquals(maxStep, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(100, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("aidomari_bridge"))
    }

    @Test
    fun entityConversion_roundTripPreservesData() {
        val domain = SugorokuProgress(
            id = "test_progress",
            currentStepIndex = 10,
            currentProgressInStep = 0,
            targetQuestionsPerStep = 1,
            totalQuestionsAnswered = 10,
            visitedNodeIds = setOf("hakodate_station", "goryokaku_park", "oshamambe", "raiden_onsen"),
            updatedAt = 123456789L,
        )

        val entity = SugorokuProgressEntity.fromDomain(domain)
        val backToDomain = entity.toDomain()

        assertEquals(domain.id, backToDomain.id)
        assertEquals(domain.currentStepIndex, backToDomain.currentStepIndex)
        assertEquals(domain.currentProgressInStep, backToDomain.currentProgressInStep)
        assertEquals(domain.targetQuestionsPerStep, backToDomain.targetQuestionsPerStep)
        assertEquals(domain.totalQuestionsAnswered, backToDomain.totalQuestionsAnswered)
        assertEquals(domain.visitedNodeIds, backToDomain.visitedNodeIds)
        assertEquals(domain.updatedAt, backToDomain.updatedAt)
    }

    @Test
    fun entityConversion_handlesMalformedOrEmptyJson() {
        val malformedEntity = SugorokuProgressEntity(
            id = "test",
            visitedNodeIdsJson = "",
        )
        val domain = malformedEntity.toDomain()
        assertEquals(setOf("hakodate_station"), domain.visitedNodeIds)
    }
}
