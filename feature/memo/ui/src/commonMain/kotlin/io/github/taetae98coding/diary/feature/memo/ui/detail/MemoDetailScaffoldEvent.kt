package io.github.taetae98coding.diary.feature.memo.ui.detail

internal sealed interface MemoDetailScaffoldEvent {
    data object ClickNavigateUp : MemoDetailScaffoldEvent

    data object ClickUpdate : MemoDetailScaffoldEvent

    data object ClickGemini : MemoDetailScaffoldEvent

    data object ClickFinish : MemoDetailScaffoldEvent

    data object ClickRestart : MemoDetailScaffoldEvent

    data object ClickCopy : MemoDetailScaffoldEvent

    data object ClickDelete : MemoDetailScaffoldEvent
}
