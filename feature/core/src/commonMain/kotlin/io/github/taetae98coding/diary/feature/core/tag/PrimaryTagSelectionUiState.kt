package io.github.taetae98coding.diary.feature.core.tag

import kotlin.uuid.Uuid

public data class PrimaryTagSelectionUiState(
    val tagIdSet: Set<Uuid> = emptySet(),
    val primaryTagId: Uuid? = null,
)
