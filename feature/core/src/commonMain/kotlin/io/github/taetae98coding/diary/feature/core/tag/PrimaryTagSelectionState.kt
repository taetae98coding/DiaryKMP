@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.tag

import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

public class PrimaryTagSelectionState(
    initialPrimaryTagId: Uuid?,
    getSelectedTag: (tagIdSet: Set<Uuid>) -> Flow<Result<List<Tag>>>,
) {
    public val selectionUiState: StateFlow<PrimaryTagSelectionUiState>
        field =
        MutableStateFlow(
            PrimaryTagSelectionUiState(
                tagIdSet = setOfNotNull(initialPrimaryTagId),
                primaryTagId = initialPrimaryTagId,
            ),
        )

    private val selectedTagList: Flow<List<Tag>> =
        selectionUiState
            .flatMapLatest { value -> getSelectedTag(value.tagIdSet) }
            .map { result -> result.getOrNull().orEmpty() }

    public val uiState: Flow<PrimaryTagInputUiState> =
        combine(selectedTagList, selectionUiState) { selectedTagList, selection ->
            PrimaryTagInputUiState(
                selectedTagList = selectedTagList,
                primaryTagId = selection.primaryTagId?.takeIf { tagId -> selectedTagList.any { tag -> tag.id == tagId } },
            )
        }

    public fun selectTag(tagId: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet + tagId) }
    }

    public fun unselectTag(tagId: Uuid) {
        selectionUiState.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet - tagId,
                primaryTagId = value.primaryTagId?.takeIf { primaryTagId -> primaryTagId != tagId },
            )
        }
    }

    public fun selectPrimaryTag(tagId: Uuid) {
        selectionUiState.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet + tagId,
                primaryTagId = tagId,
            )
        }
    }

    public fun unselectPrimaryTag() {
        selectionUiState.update { value -> value.copy(primaryTagId = null) }
    }
}
