package io.github.taetae98coding.diary.feature.memo.ui.detail

import kotlin.uuid.Uuid

internal sealed interface MemoDetailEffect {
    data object UpdateSucceeded : MemoDetailEffect

    data class CopySucceeded(
        val id: Uuid,
    ) : MemoDetailEffect

    data object DeleteSucceeded : MemoDetailEffect
}
