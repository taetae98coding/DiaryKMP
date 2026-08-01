package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence

internal sealed interface MemoHomeFilterBottomSheetEvent {
    data class SetDateExistence(
        val existence: MemoFilterExistence,
    ) : MemoHomeFilterBottomSheetEvent

    data class SetTagExistence(
        val existence: MemoFilterExistence,
    ) : MemoHomeFilterBottomSheetEvent

    data class SetPlaceExistence(
        val existence: MemoFilterExistence,
    ) : MemoHomeFilterBottomSheetEvent
}
