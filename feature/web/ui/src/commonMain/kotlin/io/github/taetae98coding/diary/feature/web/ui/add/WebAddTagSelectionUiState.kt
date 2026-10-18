package io.github.taetae98coding.diary.feature.web.ui.add

import kotlin.uuid.Uuid

internal data class WebAddTagSelectionUiState(
    val tagIdSet: Set<Uuid> = emptySet(),
)
