package io.github.taetae98coding.diary.feature.core.tag

import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagPickerEvent

public fun handlePrimaryTagPickerEvent(
    event: PrimaryTagPickerEvent,
    tagViewModel: PrimaryTagInputController,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is PrimaryTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is PrimaryTagPickerEvent.Select -> tagViewModel.selectTag(tagId = event.id)
        is PrimaryTagPickerEvent.Unselect -> tagViewModel.unselectTag(tagId = event.id)
        is PrimaryTagPickerEvent.SelectPrimary -> tagViewModel.selectPrimaryTag(tagId = event.id)
        is PrimaryTagPickerEvent.UnselectPrimary -> tagViewModel.unselectPrimaryTag()
        is PrimaryTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}
