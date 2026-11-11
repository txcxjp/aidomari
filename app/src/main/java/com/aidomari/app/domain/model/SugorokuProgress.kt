package com.aidomari.app.domain.model

data class SugorokuProgress(
    val id: String = "default_progress",
    val currentStepIndex: Int = 0,
    val currentProgressInStep: Int = 0,
    val targetQuestionsPerStep: Int = 1,
    val totalQuestionsAnswered: Int = 0,
    val visitedNodeIds: Set<String> = setOf("hakodate_station"),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val progressRatioInStep: Float
        get() = if (targetQuestionsPerStep > 0) {
            (currentProgressInStep.toFloat() / targetQuestionsPerStep).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun advanceByQuestions(
        questionsDelta: Int,
        maxStepIndex: Int = HokkaidoAct1Route.maxStepIndex,
    ): SugorokuProgress {
        if (questionsDelta <= 0) return this

        val target = targetQuestionsPerStep.coerceAtLeast(1)
        val totalAnswered = totalQuestionsAnswered + questionsDelta
        val accumulated = currentProgressInStep + questionsDelta
        val stepsToAdvance = accumulated / target
        val remainder = accumulated % target

        val newStep = (currentStepIndex + stepsToAdvance).coerceAtMost(maxStepIndex)
        val newVisited = visitedNodeIds.toMutableSet()
        for (s in currentStepIndex..newStep) {
            HokkaidoAct1Route.getNodeByStepIndex(s)?.let {
                newVisited.add(it.id)
            }
        }

        val finalProgressInStep = if (newStep >= maxStepIndex) 0 else remainder

        return copy(
            currentStepIndex = newStep,
            currentProgressInStep = finalProgressInStep,
            totalQuestionsAnswered = totalAnswered,
            visitedNodeIds = newVisited,
            updatedAt = System.currentTimeMillis(),
        )
    }
}
