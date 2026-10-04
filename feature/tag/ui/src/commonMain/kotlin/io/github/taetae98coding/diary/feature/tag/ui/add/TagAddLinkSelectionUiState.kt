package io.github.taetae98coding.diary.feature.tag.ui.add

import kotlin.uuid.Uuid

internal data class TagAddLinkSelectionUiState(
    val linkedTagIdSet: Set<Uuid> = emptySet(),
)
