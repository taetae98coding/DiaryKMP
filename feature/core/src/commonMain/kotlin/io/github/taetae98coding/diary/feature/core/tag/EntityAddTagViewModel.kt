@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
public class EntityAddTagViewModel(
    @InjectedParam initialTagId: Uuid?,
    pageTagUseCase: PageTagUseCase,
    getSelectedTagUseCase: GetSelectedTagUseCase,
) : ViewModel(),
    EntityTagInputController {
    private val pagingState = TagPickerPagingState(pageTag = { query -> pageTagUseCase(parameter = query) })

    public val tagPagingData: Flow<PagingData<Tag>> = pagingState.tagPagingData.cachedIn(viewModelScope)
    public val selectableTagPagingData: Flow<PagingData<Tag>> = pagingState.selectableTagPagingData.cachedIn(viewModelScope)

    public val selectionUiState: StateFlow<EntityAddTagSelectionUiState>
        field = MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = setOfNotNull(initialTagId)))

    public val uiState: StateFlow<EntityTagInputUiState> =
        selectionUiState
            .flatMapLatest { value -> getSelectedTagUseCase(parameter = value.tagIdSet) }
            .map { result -> EntityTagInputUiState(tagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityTagInputUiState(),
            )

    override fun updateQuery(query: String) {
        pagingState.updateQuery(query = query)
    }

    override fun add(tagId: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet + tagId) }
    }

    override fun remove(tagId: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet - tagId) }
    }
}
