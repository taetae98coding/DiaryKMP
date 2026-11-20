package io.github.taetae98coding.diary.feature.memo.ui.contact

internal fun handleMemoContactPickerEvent(
    event: MemoContactPickerEvent,
    contactViewModel: MemoContactInputController,
    navigateToContactAdd: () -> Unit,
) {
    when (event) {
        is MemoContactPickerEvent.ClickAdd -> navigateToContactAdd()
        is MemoContactPickerEvent.Select -> contactViewModel.selectContact(contactId = event.id)
        is MemoContactPickerEvent.Unselect -> contactViewModel.unselectContact(contactId = event.id)
        is MemoContactPickerEvent.ChangeQuery -> contactViewModel.updateQuery(query = event.query)
    }
}
