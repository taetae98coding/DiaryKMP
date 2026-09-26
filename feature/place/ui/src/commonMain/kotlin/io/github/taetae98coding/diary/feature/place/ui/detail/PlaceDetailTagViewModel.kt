@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceSelectableTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RemovePlaceTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceReportedSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaceDetailTagViewModel(
    @InjectedParam private val id: Uuid,
    pagePlaceSelectableTagUseCase: PagePlaceSelectableTagUseCase,
    getPlaceTagUseCase: GetPlaceTagUseCase,
    private val addPlaceTagUseCase: AddPlaceTagUseCase,
    private val removePlaceTagUseCase: RemovePlaceTagUseCase,
) : ViewModel() {
    // 화면이 검색어를 알려 주기 전에는 조회하지 않는다. 기준은 debounceReportedSearchQuery를 따른다.
    private val query = MutableStateFlow<String?>(null)

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceReportedSearchQuery()
            .flatMapLatest { value ->
                pagePlaceSelectableTagUseCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = value))
            }.mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val selectableTagPagingData: Flow<PagingData<Tag>> =
        flowOf("")
            .flatMapLatest { value ->
                pagePlaceSelectableTagUseCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = value))
            }.mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<EntityTagInputUiState> =
        getPlaceTagUseCase(parameter = id)
            .map { result -> EntityTagInputUiState(tagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityTagInputUiState(),
            )

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun add(tagId: Uuid) {
        viewModelScope.launch {
            addPlaceTagUseCase(parameter = AddPlaceTagUseCase.Parameter(placeId = id, tagId = tagId))
        }
    }

    fun remove(tagId: Uuid) {
        viewModelScope.launch {
            removePlaceTagUseCase(parameter = RemovePlaceTagUseCase.Parameter(placeId = id, tagId = tagId))
        }
    }
}
