package io.github.taetae98coding.diary.feature.core.tag

import kotlin.uuid.Uuid

public data class EntityAddTagSelectionUiState(
    val tagIdSet: Set<Uuid> = emptySet(),
)
