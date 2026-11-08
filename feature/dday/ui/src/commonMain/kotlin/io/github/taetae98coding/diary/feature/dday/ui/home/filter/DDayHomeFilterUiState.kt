package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import kotlin.uuid.Uuid

internal data class DDayHomeFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
)
