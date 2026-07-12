package com.bloom.app.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import com.bloom.app.ai.ReflectionState
import com.bloom.app.data.model.JournalEntry
import com.bloom.app.data.model.Mood
import com.bloom.app.util.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// JournalEditorUiState
//
// Represents the entire editor screen state as an immutable snapshot.
// The ViewModel produces these; the UI only reads them.
// ─────────────────────────────────────────────────────────────────────────────

data class JournalEditorUiState(
    val entryId             : Long             = -1L,
    val content             : String           = "",
    val originalContent     : String           = "",
    val selectedMood        : Mood             = Mood.OKAY,
    val originalMood        : Mood             = Mood.OKAY,
    val activePrompt        : String?          = null,
    val aiReflection        : String?          = null,
    val reflectionState     : ReflectionState? = null,
    val isSaved             : Boolean          = false,
    val isLoading           : Boolean          = false,
    val wordCount           : Int              = 0,
    val isExistingEntry     : Boolean          = false,
    /** Preserved from original entry so edits don't corrupt creation date */
    val originalCreatedAt   : Long             = 0L,
)

data class JournalListUiState(
    val entries     : List<JournalEntry> = emptyList(),
    val isLoading   : Boolean            = true,
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val container   = (application as BloomApplication).container
    private val journalRepo = container.journalRepository
    private val gemini      = container.geminiService

    // ── Editor State ──────────────────────────────────────────────────────────
    private val _editorState = MutableStateFlow(JournalEditorUiState())
    val editorState: StateFlow<JournalEditorUiState> = _editorState.asStateFlow()

    // ── List State ────────────────────────────────────────────────────────────
    val listState: StateFlow<JournalListUiState> =
        journalRepo.allEntries
            .map { entries -> JournalListUiState(entries = entries, isLoading = false) }
            .stateIn(
                scope         = viewModelScope,
                started       = SharingStarted.WhileSubscribed(5_000),
                initialValue  = JournalListUiState(),
            )

    /**
     * Load an existing entry into the editor.
     * Called when navigating to JournalEditor with a valid entryId.
     */
    fun loadEntry(entryId: Long) {
        if (entryId < 0) {
            // New entry — pick a random prompt for today
            val dayOfYear = LocalDate.now().dayOfYear
            val prompt = Constants.JOURNAL_PROMPTS[dayOfYear % Constants.JOURNAL_PROMPTS.size]
            _editorState.value = JournalEditorUiState(
                activePrompt    = prompt,
                isExistingEntry = false,
            )
            return
        }

        viewModelScope.launch {
            _editorState.value = _editorState.value.copy(isLoading = true)
            journalRepo.observeEntry(entryId)
                .filterNotNull()
                .first()
                .let { entry ->
                    _editorState.value = JournalEditorUiState(
                        entryId           = entry.id,
                        content           = entry.content,
                        originalContent   = entry.content,
                        selectedMood      = entry.mood,
                        originalMood      = entry.mood,
                        activePrompt      = entry.prompt,
                        aiReflection      = entry.aiReflection,
                        wordCount         = entry.wordCount,
                        originalCreatedAt = entry.createdAt,   // ✅ preserved
                        isLoading         = false,
                        isExistingEntry   = true,
                    )
                }
        }
    }

    fun onContentChanged(text: String) {
        val wordCount = text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
        _editorState.value = _editorState.value.copy(
            content   = text,
            wordCount = wordCount,
        )
    }

    fun onMoodSelected(mood: Mood) {
        _editorState.value = _editorState.value.copy(selectedMood = mood)
    }

    fun saveEntry() {
        val state = _editorState.value
        if (state.content.isBlank()) return

        val contentChanged = state.content != state.originalContent || state.selectedMood != state.originalMood

        viewModelScope.launch {
            if (state.isExistingEntry && state.entryId >= 0) {
                // Preserve the original createdAt — only updatedAt changes
                journalRepo.updateEntry(
                    JournalEntry(
                        id        = state.entryId,
                        content   = state.content,
                        mood      = state.selectedMood,
                        prompt    = state.activePrompt,
                        wordCount = state.wordCount,
                        createdAt = state.originalCreatedAt,  // preserved!
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            } else {
                journalRepo.saveEntry(
                    content = state.content,
                    mood    = state.selectedMood,
                    prompt  = state.activePrompt,
                )
            }
            
            if (contentChanged) {
                com.bloom.app.util.AppEventBus.emit(com.bloom.app.util.AppEvent.JournalEntrySaved)
            }

            _editorState.value = _editorState.value.copy(
                isSaved = true,
                originalContent = state.content,
                originalMood = state.selectedMood
            )
        }
    }

    /**
     * Request a Gemini reflection for the current entry content.
     * The entry must be saved first — we only reflect on committed writing.
     */
    fun requestReflection() {
        val state = _editorState.value
        if (state.content.isBlank()) return

        viewModelScope.launch {
            // Save first if it's a new entry
            var entryId = state.entryId
            if (!state.isExistingEntry || entryId < 0) {
                entryId = journalRepo.saveEntry(
                    content = state.content,
                    mood    = state.selectedMood,
                    prompt  = state.activePrompt,
                )
                
                com.bloom.app.util.AppEventBus.emit(com.bloom.app.util.AppEvent.JournalEntrySaved)
                
                _editorState.value = _editorState.value.copy(
                    entryId         = entryId,
                    isExistingEntry = true,
                    originalContent = state.content,
                    originalMood    = state.selectedMood
                )
            }

            // Stream reflection
            gemini.generateReflection(
                entryContent = state.content,
                mood         = state.selectedMood.displayName,
                prompt       = state.activePrompt,
            ).collect { reflectionState ->
                _editorState.value = _editorState.value.copy(
                    reflectionState = reflectionState,
                )

                // When streaming is complete, persist the reflection
                if (reflectionState is ReflectionState.Streaming) {
                    val finalText = reflectionState.text
                    // Update local state
                    _editorState.value = _editorState.value.copy(
                        aiReflection    = finalText,
                    )
                    // Persist to Room (debounced — only on final chunk)
                    // We detect "final" by checking if the flow has ended,
                    // which happens in the complete emission below
                }
            }

            // After flow completes, save the full reflection text
            val finalReflection = _editorState.value.aiReflection
            if (finalReflection != null && entryId >= 0) {
                journalRepo.saveReflection(entryId, finalReflection)
            }
        }
    }
}
