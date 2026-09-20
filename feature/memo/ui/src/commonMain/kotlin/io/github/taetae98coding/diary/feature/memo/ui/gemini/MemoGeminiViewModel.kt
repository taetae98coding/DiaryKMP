package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.usecase.GetGeminiSettingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MemoGeminiViewModel(
    getGeminiSettingUseCase: GetGeminiSettingUseCase,
    private val fetchMemoDraftUseCase: FetchMemoDraftUseCase,
) : ViewModel() {
    private val assistantState = MutableStateFlow(MemoGeminiAssistantState())
    private var generateJob: Job? = null

    private val setting: StateFlow<GeminiSetting?> =
        getGeminiSettingUseCase(parameter = Unit)
            .map { result -> result.getOrNull() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
                initialValue = null,
            )

    private val _effect = Channel<MemoGeminiEffect>(Channel.BUFFERED)
    val effect: Flow<MemoGeminiEffect> = _effect.receiveAsFlow()

    val uiState: StateFlow<MemoGeminiUiState> =
        combine(setting, assistantState) { setting, assistant ->
            MemoGeminiUiState(
                isButtonVisible = setting != null,
                step = assistant.step,
                draft = assistant.draft,
                appliedFieldSet = assistant.appliedFieldSet,
                failure = assistant.failure,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            initialValue = MemoGeminiUiState(),
        )

    fun open() {
        val setting = setting.value ?: return

        if (setting.apiKey.isBlank() || setting.model.isBlank()) {
            viewModelScope.launch { _effect.send(MemoGeminiEffect.SettingRequired) }
            return
        }

        assistantState.value = MemoGeminiAssistantState(step = MemoGeminiStep.PROMPT)
    }

    fun close() {
        generateJob?.cancel()
        assistantState.value = MemoGeminiAssistantState()
    }

    fun cancel() {
        generateJob?.cancel()
        assistantState.update { value -> value.copy(step = MemoGeminiStep.PROMPT) }
    }

    fun generate(parameter: FetchMemoDraftUseCase.Parameter) {
        if (assistantState.value.step == MemoGeminiStep.GENERATING) return

        assistantState.update { value -> value.copy(step = MemoGeminiStep.GENERATING, failure = null) }
        generateJob =
            viewModelScope.launch {
                fetchMemoDraftUseCase(parameter = parameter)
                    .onSuccess { draft -> onGenerateSuccess(draft = draft) }
                    .onFailure { throwable -> onGenerateFailure(throwable = throwable) }
            }
    }

    fun markApplied(field: MemoGeminiField) {
        assistantState.update { value -> value.copy(appliedFieldSet = value.appliedFieldSet + field) }
    }

    private fun onGenerateSuccess(draft: MemoDraft) {
        assistantState.update { value ->
            value.copy(
                step = MemoGeminiStep.RESULT,
                draft = draft,
                appliedFieldSet = emptySet(),
            )
        }
    }

    private fun onGenerateFailure(throwable: Throwable) {
        val failure =
            if (throwable is GeminiApiKeyInvalidException) {
                MemoGeminiFailure.INVALID_API_KEY
            } else {
                MemoGeminiFailure.UNKNOWN
            }

        assistantState.update { value -> value.copy(step = MemoGeminiStep.PROMPT, failure = failure) }
    }

    private data class MemoGeminiAssistantState(
        val step: MemoGeminiStep = MemoGeminiStep.CLOSED,
        val draft: MemoDraft = MemoDraft.EMPTY,
        val appliedFieldSet: Set<MemoGeminiField> = emptySet(),
        val failure: MemoGeminiFailure? = null,
    )

    private companion object {
        private const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
