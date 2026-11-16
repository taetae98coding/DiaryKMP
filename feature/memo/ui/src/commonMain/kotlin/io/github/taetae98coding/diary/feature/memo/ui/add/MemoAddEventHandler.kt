package io.github.taetae98coding.diary.feature.memo.ui.add

import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel

internal fun handleMemoAddEvent(
    event: MemoAddScaffoldEvent,
    addViewModel: MemoAddViewModel,
    tagViewModel: MemoAddTagViewModel,
    webViewModel: MemoAddWebViewModel,
    contactViewModel: MemoAddContactViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    geminiViewModel: MemoGeminiViewModel,
    scaffoldState: MemoFormState,
    navigateUp: () -> Unit,
) {
    when (event) {
        is MemoAddScaffoldEvent.ClickNavigateUp -> navigateUp()

        is MemoAddScaffoldEvent.ClickAdd ->
            addViewModel.add(
                detail = scaffoldState.detail,
                tagSelection = tagViewModel.selectionUiState.value,
                webIdSet = webViewModel.selectionUiState.value.webIdSet,
                contactIdSet = contactViewModel.selectionUiState.value.contactIdSet,
                placeIdSet = placeViewModel.selectionUiState.value.placeIdSet,
            )

        is MemoAddScaffoldEvent.ClickGemini -> geminiViewModel.open()
    }
}
