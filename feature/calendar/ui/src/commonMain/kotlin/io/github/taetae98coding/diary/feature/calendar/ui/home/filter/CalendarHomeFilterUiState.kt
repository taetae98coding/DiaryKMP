package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import kotlin.uuid.Uuid

internal data class CalendarHomeFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
)
