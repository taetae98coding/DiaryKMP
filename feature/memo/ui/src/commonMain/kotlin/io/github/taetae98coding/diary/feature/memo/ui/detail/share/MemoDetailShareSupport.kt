package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import androidx.compose.runtime.Immutable

@Immutable
internal data class MemoDetailShareSupport(
    val isTextSupported: Boolean = false,
    val isCalendarFileSupported: Boolean = false,
)
