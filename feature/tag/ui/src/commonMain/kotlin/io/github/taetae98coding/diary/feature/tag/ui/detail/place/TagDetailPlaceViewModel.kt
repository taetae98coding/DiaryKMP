@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetTagPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailPlaceViewModel(
    @InjectedParam tagId: Uuid,
    getTagPlaceListUseCase: GetTagPlaceListUseCase,
    pageTagPlaceUseCase: PageTagPlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) : ViewModel() {
    private val visibleBounds = MutableStateFlow<CoordinateBounds?>(null)

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val scope: StateFlow<TagScope>
        field = MutableStateFlow(TagScope.SELF)

    val placeListUiState: StateFlow<TagDetailPlaceListUiState> =
        combine(visibleBounds, sort, scope) { bounds, sortValue, scopeValue -> Triple(bounds, sortValue, scopeValue) }
            .flatMapLatest { (bounds, sortValue, scopeValue) ->
                if (bounds == null) {
                    flowOf(TagDetailPlaceListUiState())
                } else {
                    getTagPlaceListUseCase(
                        parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = scopeValue, bounds = bounds, sort = sortValue),
                    ).map { result ->
                        TagDetailPlaceListUiState(
                            isLoaded = true,
                            placeList = result.getOrDefault(emptyList()),
                        )
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagDetailPlaceListUiState(),
            )

    val placePagingData: Flow<PagingData<Place>> =
        combine(sort, scope) { sortValue, scopeValue -> sortValue to scopeValue }
            .flatMapLatest { (sortValue, scopeValue) ->
                pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = scopeValue, sort = sortValue))
                    .runningFold<Result<PagingData<Place>>, PagingData<Place>?>(initial = null) { last, result ->
                        result.getOrElse { last ?: PagingData.empty() }
                    }.filterNotNull()
                    .distinctUntilChanged()
            }.cachedIn(viewModelScope)

    private val _effect = Channel<PlaceListEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceListEffect> = _effect.receiveAsFlow()

    fun updateVisibleBounds(bounds: CoordinateBounds?) {
        visibleBounds.value = bounds
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun select(scope: TagScope) {
        this.scope.value = scope
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deletePlaceUseCase(parameter = id)
                .onSuccess { _effect.send(PlaceListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restorePlaceUseCase(parameter = id)
        }
    }
}
