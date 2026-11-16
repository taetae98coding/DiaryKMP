package io.github.taetae98coding.diary.feature.memo.ui.tag

internal fun handleMemoTagPickerEvent(
    event: MemoTagPickerEvent,
    tagViewModel: MemoTagInputController,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is MemoTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is MemoTagPickerEvent.Select -> tagViewModel.selectTag(tagId = event.id)
        is MemoTagPickerEvent.Unselect -> tagViewModel.unselectTag(tagId = event.id)
        is MemoTagPickerEvent.SelectPrimary -> tagViewModel.selectPrimaryTag(tagId = event.id)
        is MemoTagPickerEvent.UnselectPrimary -> tagViewModel.unselectPrimaryTag()
        is MemoTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}
