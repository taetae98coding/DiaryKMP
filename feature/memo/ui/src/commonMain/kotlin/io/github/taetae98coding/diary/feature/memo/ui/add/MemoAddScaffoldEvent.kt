package io.github.taetae98coding.diary.feature.memo.ui.add

internal sealed interface MemoAddScaffoldEvent {
    data object ClickNavigateUp : MemoAddScaffoldEvent

    data object ClickAdd : MemoAddScaffoldEvent

    data object ClickGemini : MemoAddScaffoldEvent
}
