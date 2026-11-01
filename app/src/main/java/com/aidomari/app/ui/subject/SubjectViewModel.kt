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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SubjectViewModel(
    private val repository: WordsRepository,
    private val importer: WordsDataImporter = WordsDataImporter(repository),
    private val exporter: WordsDataExporter = WordsDataExporter(repository),
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun createSubject(name: String, ttsLanguage: String = "auto") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newSubject = Subject(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                ttsLanguage = ttsLanguage,
                sortOrder = subjects.value.size,
            )
            repository.saveSubject(newSubject)
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
