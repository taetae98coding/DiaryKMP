package io.github.taetae98coding.diary.compose.tag.primary

import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlin.uuid.Uuid

public data class PrimaryTagInputUiState(
    val selectedTagList: List<Tag> = emptyList(),
    val primaryTagId: Uuid? = null,
)
