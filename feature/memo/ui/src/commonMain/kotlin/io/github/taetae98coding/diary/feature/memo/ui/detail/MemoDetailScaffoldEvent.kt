package io.github.taetae98coding.diary.feature.memo.ui.detail

internal sealed interface MemoDetailScaffoldEvent {
    data object ClickNavigateUp : MemoDetailScaffoldEvent

    data object ClickUpdate : MemoDetailScaffoldEvent

    data object ClickGemini : MemoDetailScaffoldEvent

    data object ClickFinish : MemoDetailScaffoldEvent

    data object ClickRestart : MemoDetailScaffoldEvent

    data object ClickCopy : MemoDetailScaffoldEvent

    data object ClickDelete : MemoDetailScaffoldEvent

    sealed interface Share : MemoDetailScaffoldEvent

    data object ClickShare : Share

    data object ClickShareText : Share

    data object ClickShareCalendarFile : Share

    data object DismissShareSheet : Share
}
