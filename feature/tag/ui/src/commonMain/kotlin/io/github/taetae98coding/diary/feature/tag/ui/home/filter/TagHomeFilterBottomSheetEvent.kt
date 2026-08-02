package io.github.taetae98coding.diary.feature.tag.ui.home.filter

internal sealed interface TagHomeFilterBottomSheetEvent {
    data class SetTopLevelOnly(
        val isTopLevelOnly: Boolean,
    ) : TagHomeFilterBottomSheetEvent
}
