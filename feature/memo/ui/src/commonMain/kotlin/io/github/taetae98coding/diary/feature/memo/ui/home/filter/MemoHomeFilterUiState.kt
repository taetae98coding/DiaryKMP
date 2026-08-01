package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import kotlin.uuid.Uuid

internal data class MemoHomeFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val existence: MemoExistenceFilter = MemoExistenceFilter(),
)

internal fun MemoHomeFilterUiState.isTagFilterEnabled(): Boolean = existence.tag != MemoFilterExistence.NOT_EXIST
