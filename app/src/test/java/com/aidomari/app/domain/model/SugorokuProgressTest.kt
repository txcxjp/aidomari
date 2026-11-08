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
        assertEquals(setOf("node_0000"), progress.visitedNodeIds)
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
        assertTrue(advanced.visitedNodeIds.contains("node_0000"))
        assertTrue(advanced.visitedNodeIds.contains("node_0001"))
    }

    @Test
    fun advanceByQuestions_subStepIncrementsProgressOnlyWhenTargetGreaterThanOne() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 5)
        val advanced = initial.advanceByQuestions(3)

        assertEquals(0, advanced.currentStepIndex)
        assertEquals(3, advanced.currentProgressInStep)
        assertEquals(3, advanced.totalQuestionsAnswered)
        assertEquals(setOf("node_0000"), advanced.visitedNodeIds)
    }

    @Test
    fun advanceByQuestions_multiStep_advancesMultipleAndCollectsAllVisitedNodes() {
        val initial = SugorokuProgress(targetQuestionsPerStep = 1)
        val advanced = initial.advanceByQuestions(3) // 3 steps

        assertEquals(3, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(3, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("node_0000"))
        assertTrue(advanced.visitedNodeIds.contains("node_0001"))
        assertTrue(advanced.visitedNodeIds.contains("node_0002"))
        assertTrue(advanced.visitedNodeIds.contains("node_0003"))
        assertFalse(advanced.visitedNodeIds.contains("node_0004"))
    }

    @Test
    fun advanceByQuestions_capsAtGoal() {
        val initial = SugorokuProgress(
            currentStepIndex = 1995,
            currentProgressInStep = 0,
            targetQuestionsPerStep = 1,
        )
        val advanced = initial.advanceByQuestions(100)

        assertEquals(1999, advanced.currentStepIndex)
        assertEquals(0, advanced.currentProgressInStep)
        assertEquals(100, advanced.totalQuestionsAnswered)
        assertTrue(advanced.visitedNodeIds.contains("node_1999"))
    }

    @Test
    fun entityConversion_roundTripPreservesData() {
        val domain = SugorokuProgress(
            id = "test_progress",
            currentStepIndex = 10,
            currentProgressInStep = 0,
            targetQuestionsPerStep = 1,
            totalQuestionsAnswered = 10,
            visitedNodeIds = setOf("node_0000", "node_0001", "node_0005", "node_0010"),
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
        assertEquals(setOf("node_0000"), domain.visitedNodeIds)
    }
}
