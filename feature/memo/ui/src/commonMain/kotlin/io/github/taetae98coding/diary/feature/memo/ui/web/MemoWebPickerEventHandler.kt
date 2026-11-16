package io.github.taetae98coding.diary.feature.memo.ui.web

internal fun handleMemoWebPickerEvent(
    event: MemoWebPickerEvent,
    webViewModel: MemoWebInputController,
    navigateToWebAdd: () -> Unit,
) {
    when (event) {
        is MemoWebPickerEvent.ClickAdd -> navigateToWebAdd()
        is MemoWebPickerEvent.Select -> webViewModel.selectWeb(webId = event.id)
        is MemoWebPickerEvent.Unselect -> webViewModel.unselectWeb(webId = event.id)
        is MemoWebPickerEvent.ChangeQuery -> webViewModel.updateQuery(query = event.query)
    }
}
