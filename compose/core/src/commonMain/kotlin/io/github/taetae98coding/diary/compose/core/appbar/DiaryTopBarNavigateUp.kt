package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Immutable

@Immutable
internal data class DiaryTopBarNavigateUp(
    val contentDescription: String?,
    val onClick: () -> Unit,
)
