package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.exception.GeminiRequestException
import io.github.taetae98coding.diary.domain.setting.usecase.GetGeminiSettingUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
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
        getGeminiSettingUseCase()
            .map { result -> result.getOrNull() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
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
                progress = assistant.progress,
                failure = assistant.failure,
                failureDescription = assistant.failureDescription,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
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
        assistantState.update { value -> value.copy(step = MemoGeminiStep.PROMPT, progress = MemoDraftProgress.EMPTY) }
    }

    fun generate(parameter: FetchMemoDraftUseCase.Parameter) {
        if (assistantState.value.step == MemoGeminiStep.GENERATING) return

        assistantState.update { value -> value.copy(step = MemoGeminiStep.GENERATING, progress = MemoDraftProgress.EMPTY, failure = null, failureDescription = "") }
        generateJob =
            viewModelScope.launch {
                fetchMemoDraftUseCase(parameter = parameter.copy(onProgress = ::onGenerateProgress))
                    .onSuccess { draft -> onGenerateSuccess(draft = draft) }
                    .onFailure { throwable -> onGenerateFailure(throwable = throwable) }
            }
    }

    fun markApplied(field: MemoGeminiField) {
        assistantState.update { value -> value.copy(appliedFieldSet = value.appliedFieldSet + field) }
    }

    private fun onGenerateProgress(progress: MemoDraftProgress) {
        assistantState.update { value -> value.copy(progress = progress) }
    }

    private fun onGenerateSuccess(draft: MemoDraft) {
        assistantState.update { value ->
            value.copy(
                step = MemoGeminiStep.RESULT,
                draft = draft,
                progress = MemoDraftProgress.EMPTY,
                appliedFieldSet = emptySet(),
            )
        }
    }

    private fun onGenerateFailure(throwable: Throwable) {
        assistantState.update { value ->
            value.copy(
                step = MemoGeminiStep.PROMPT,
                progress = MemoDraftProgress.EMPTY,
                failure = throwable.toMemoGeminiFailure(),
                failureDescription = (throwable as? GeminiRequestException.Other)?.description.orEmpty(),
            )
        }
    }

    private fun Throwable.toMemoGeminiFailure(): MemoGeminiFailure =
        when (this) {
            is GeminiApiKeyInvalidException -> MemoGeminiFailure.INVALID_API_KEY
            is GeminiRequestException.QuotaExceeded -> MemoGeminiFailure.QUOTA_EXCEEDED
            is GeminiRequestException.ModelNotFound -> MemoGeminiFailure.MODEL_NOT_FOUND
            is GeminiRequestException.Blocked -> MemoGeminiFailure.BLOCKED
            is GeminiRequestException.Truncated -> MemoGeminiFailure.TRUNCATED
            is GeminiRequestException.Timeout -> MemoGeminiFailure.TIMEOUT
            is GeminiRequestException.Network -> MemoGeminiFailure.NETWORK
            is GeminiRequestException.Server -> MemoGeminiFailure.SERVER
            else -> MemoGeminiFailure.UNKNOWN
        }

    private data class MemoGeminiAssistantState(
        val step: MemoGeminiStep = MemoGeminiStep.CLOSED,
        val draft: MemoDraft = MemoDraft.EMPTY,
        val appliedFieldSet: Set<MemoGeminiField> = emptySet(),
        val progress: MemoDraftProgress = MemoDraftProgress.EMPTY,
        val failure: MemoGeminiFailure? = null,
        val failureDescription: String = "",
    )
}
