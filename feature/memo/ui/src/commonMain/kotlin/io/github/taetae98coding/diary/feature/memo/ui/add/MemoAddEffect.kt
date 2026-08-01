package io.github.taetae98coding.diary.feature.memo.ui.add

internal sealed interface MemoAddEffect {
    data object AddSucceeded : MemoAddEffect

    data object TitleBlank : MemoAddEffect
}
