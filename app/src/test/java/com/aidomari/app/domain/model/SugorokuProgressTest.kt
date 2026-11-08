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
        assertEquals(5, progress.targetQuestionsPerStep)
        assertEquals(0, progress.totalQuestionsAnswered)
        assertEquals(setOf("node_000"), progress.visitedNodeIds)
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
    fun advanceByQuestions_subStepIncrementsProgressOnly() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 5)
        val advanced = initial.advanceByQuestions(3)

        assertEquals(0, advanced.currentStepIndex)
        assertEquals(3, advanced.currentProgressInStep)
        assertEquals(3, advanced.totalQuestionsAnswered)
        assertEquals(setOf("node_000"), advanced.visitedNodeIds)
    }

    @Test
    fun advanceByQuestions_exactStep_advancesOneStepAndResetsRemainder() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 5)
        val advanced = initial.advanceByQuestions(5)

        assertEquals(1, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(5, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("node_000"))
        assertTrue(advanced.visitedNodeIds.contains("node_001"))
    }

    @Test
    fun advanceByQuestions_multiStep_advancesMultipleAndCollectsAllVisitedNodes() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 5)
        val advanced = initial.advanceByQuestions(12) // 2 steps (5*2) + 2 remainder

        assertEquals(2, advanced.currentStepIndex)
        assertEquals(2, advanced.currentProgressInStep)
        assertEquals(12, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("node_000"))
        assertTrue(advanced.visitedNodeIds.contains("node_001"))
        assertTrue(advanced.visitedNodeIds.contains("node_002"))
        assertFalse(advanced.visitedNodeIds.contains("node_003"))
    }

    @Test
    fun advanceByQuestions_capsAtGoal() {
        val initial = SugorokuProgress(
            currentStepIndex = 111,
            currentProgressInStep = 3,
            targetQuestionsPerStep = 5,
        )
        // Need 2 more for step 112 (相泊ゴール)
        val advanced = initial.advanceByQuestions(100)

        assertEquals(112, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(100, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("node_112"))
    }

    @Test
    fun entityConversion_roundTripPreservesData() {
        val domain = SugorokuProgress(
            id = "test_progress",
            currentStepIndex = 10,
            currentProgressInStep = 3,
            targetQuestionsPerStep = 5,
            totalQuestionsAnswered = 53,
            visitedNodeIds = setOf("node_000", "node_001", "node_005", "node_010"),
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
        assertEquals(setOf("node_000"), domain.visitedNodeIds)
    }
}
