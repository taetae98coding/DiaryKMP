package io.github.taetae98coding.diary.compose.tag.entity

import io.github.taetae98coding.diary.core.model.tag.Tag

public data class EntityTagInputUiState(
    val tagList: List<Tag> = emptyList(),
)
