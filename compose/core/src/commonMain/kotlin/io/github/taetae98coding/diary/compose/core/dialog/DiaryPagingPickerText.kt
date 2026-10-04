package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.runtime.Immutable

@Immutable
public data class DiaryPagingPickerText(
    val title: String,
    val searchPlaceholder: String,
    val searchEmptyTitle: String,
    val searchEmptyDescription: String,
    val addLabel: String,
    val addActionLabel: String,
)
