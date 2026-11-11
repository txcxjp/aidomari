package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aidomari.app.domain.model.SugorokuProgress

@Entity(tableName = "sugoroku_progress")
data class SugorokuProgressEntity(
    @PrimaryKey
    val id: String = "default_progress",
    @ColumnInfo(name = "current_step_index")
    val currentStepIndex: Int = 0,
    @ColumnInfo(name = "current_progress_in_step")
    val currentProgressInStep: Int = 0,
    @ColumnInfo(name = "target_questions_per_step")
    val targetQuestionsPerStep: Int = 1,
    @ColumnInfo(name = "total_questions_answered")
    val totalQuestionsAnswered: Int = 0,
    @ColumnInfo(name = "visited_node_ids_json")
    val visitedNodeIdsJson: String = "[\"hakodate_station\"]",
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(): SugorokuProgress {
        val visitedIds = try {
            val trimmed = visitedNodeIdsJson.trim().removePrefix("[").removeSuffix("]")
            if (trimmed.isBlank()) {
                emptySet()
            } else {
                trimmed.split(",").map { it.trim().removeSurrounding("\"") }.toSet()
            }
        } catch (_: Exception) {
            emptySet()
        }
        return SugorokuProgress(
            id = id,
            currentStepIndex = currentStepIndex,
            currentProgressInStep = currentProgressInStep,
            targetQuestionsPerStep = targetQuestionsPerStep,
            totalQuestionsAnswered = totalQuestionsAnswered,
            visitedNodeIds = visitedIds.ifEmpty { setOf("hakodate_station") },
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(progress: SugorokuProgress): SugorokuProgressEntity {
            val json = progress.visitedNodeIds.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            return SugorokuProgressEntity(
                id = progress.id,
                currentStepIndex = progress.currentStepIndex,
                currentProgressInStep = progress.currentProgressInStep,
                targetQuestionsPerStep = progress.targetQuestionsPerStep,
                totalQuestionsAnswered = progress.totalQuestionsAnswered,
                visitedNodeIdsJson = json,
                updatedAt = progress.updatedAt,
            )
        }
    }
}
