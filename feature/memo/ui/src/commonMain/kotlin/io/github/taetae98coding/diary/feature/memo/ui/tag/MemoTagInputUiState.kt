package io.github.taetae98coding.diary.feature.memo.ui.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlin.uuid.Uuid

internal data class MemoTagInputUiState(
    val selectedTagList: List<Tag> = emptyList(),
    val primaryTagId: Uuid? = null,
)
