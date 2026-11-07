package com.aidomari.app.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.core.importer.ImportSummary
import com.aidomari.app.core.importer.WordsDataExporter
import com.aidomari.app.core.importer.WordsDataImporter
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SubjectViewModel(
    private val repository: WordsRepository,
    private val importer: WordsDataImporter = WordsDataImporter(repository),
    private val exporter: WordsDataExporter = WordsDataExporter(repository),
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .map { list -> list.filterNot { it.isArchived } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val archivedSubjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .map { list -> list.filter { it.isArchived } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    fun createSubject(name: String, ttsLanguage: String = "auto", colorTheme: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newSubject = Subject(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                ttsLanguage = ttsLanguage,
                sortOrder = subjects.value.size,
                colorTheme = colorTheme,
            )
            repository.saveSubject(newSubject)
        }
    }

    fun updateSubject(id: String, name: String, ttsLanguage: String = "auto", colorTheme: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val subject = repository.getSubjectById(id) ?: return@launch
            repository.saveSubject(
                subject.copy(
                    name = name.trim(),
                    ttsLanguage = ttsLanguage,
                    colorTheme = colorTheme,
                ),
            )
        }
    }

    fun archiveSubject(id: String) {
        viewModelScope.launch {
            val subject = repository.getSubjectById(id) ?: return@launch
            repository.saveSubject(subject.copy(isArchived = true))
        }
    }

    fun unarchiveSubject(id: String) {
        viewModelScope.launch {
            val subject = repository.getSubjectById(id) ?: return@launch
            repository.saveSubject(subject.copy(isArchived = false))
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun importData(
        jsonString: String,
        targetSubjectId: String? = null,
        onComplete: (Result<ImportSummary>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = importer.importJson(jsonString, targetSubjectId)
            onComplete(result)
        }
    }

    suspend fun exportData(): String = exporter.exportAll()
}
