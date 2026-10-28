@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
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
internal class PlaceAddTagViewModel(
    @InjectedParam initialTagId: Uuid?,
    pageTagUseCase: PageTagUseCase,
    getSelectedTagUseCase: GetSelectedTagUseCase,
) : ViewModel() {
    val selectionUiState: StateFlow<PlaceAddTagSelectionUiState>
        field = MutableStateFlow(PlaceAddTagSelectionUiState(tagIdSet = setOfNotNull(initialTagId)))

    private val query = MutableStateFlow("")

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> pageTagUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<EntityTagInputUiState> =
        selectionUiState
            .flatMapLatest { value -> getSelectedTagUseCase(parameter = value.tagIdSet) }
            .map { result -> EntityTagInputUiState(tagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityTagInputUiState(),
            )

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun add(id: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet + id) }
    }

    fun remove(id: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet - id) }
    }
}
