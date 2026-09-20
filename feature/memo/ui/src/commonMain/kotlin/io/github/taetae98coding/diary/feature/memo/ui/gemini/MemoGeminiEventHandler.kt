package io.github.taetae98coding.diary.feature.memo.ui.gemini

import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.toDiaryDateTimeInputValue
import io.github.taetae98coding.diary.feature.memo.ui.toMemoDateTime

internal fun handleMemoGeminiEvent(
    event: MemoGeminiDialogEvent,
    geminiViewModel: MemoGeminiViewModel,
    state: MemoFormState,
) {
    when (event) {
        is MemoGeminiDialogEvent.ClickGenerate ->
            geminiViewModel.generate(
                parameter =
                    FetchMemoDraftUseCase.Parameter(
                        prompt = event.prompt,
                        title = state.titleState.text.toString(),
                        description = state.descriptionState.text.toString(),
                        dateTime = state.dateTimeState.value.toMemoDateTime(),
                    ),
            )

        is MemoGeminiDialogEvent.ClickCancel -> geminiViewModel.cancel()

        is MemoGeminiDialogEvent.ClickApply -> {
            applyMemoGeminiField(
                field = event.field,
                geminiViewModel = geminiViewModel,
                state = state,
            )
        }
    }
}

private fun applyMemoGeminiField(
    field: MemoGeminiField,
    geminiViewModel: MemoGeminiViewModel,
    state: MemoFormState,
) {
    val draft = geminiViewModel.uiState.value.draft

    when (field) {
        MemoGeminiField.TITLE -> state.titleState.setText(draft.title)
        MemoGeminiField.DESCRIPTION -> state.descriptionState.setText(draft.description)
        MemoGeminiField.DATE_TIME -> draft.dateTime.toDiaryDateTimeInputValue()?.let(state.dateTimeState::select)
    }

    geminiViewModel.markApplied(field = field)
}
