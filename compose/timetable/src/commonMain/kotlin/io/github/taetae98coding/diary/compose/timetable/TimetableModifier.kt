package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.ui.Modifier

internal inline fun <T : Any> Modifier.thenIfNotNull(
    value: T?,
    block: Modifier.(T) -> Modifier,
): Modifier = if (value == null) this else block(value)
