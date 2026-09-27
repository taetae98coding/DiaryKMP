@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
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
internal class MemoPlaceViewModel(
    @InjectedParam private val id: Uuid,
    pagePlaceUseCase: PagePlaceUseCase,
    getMemoPlaceUseCase: GetMemoPlaceUseCase,
    private val addMemoPlaceUseCase: AddMemoPlaceUseCase,
    private val removeMemoPlaceUseCase: RemoveMemoPlaceUseCase,
) : ViewModel() {
    val uiState: StateFlow<MemoPlaceInputUiState> =
        getMemoPlaceUseCase(parameter = id)
            .map { result ->
                MemoPlaceInputUiState(
                    isSelectedPlaceLoaded = result.isSuccess,
                    selectedPlaceList = result.getOrNull().orEmpty(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoPlaceInputUiState(),
            )

    // 화면이 검색어를 알려 주기 전에는 조회하지 않는다. 기준은 debounceReportedSearchQuery를 따른다.
    private val query = MutableStateFlow<String?>(null)

    val placePagingData: Flow<PagingData<Place>> =
        query
            .debounceReportedSearchQuery()
            .flatMapLatest { value -> pagePlaceUseCase(parameter = value) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val selectablePlacePagingData: Flow<PagingData<Place>> =
        flowOf("")
            .flatMapLatest { query -> pagePlaceUseCase(parameter = query) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectPlace(placeId: Uuid) {
        viewModelScope.launch {
            addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId))
        }
    }

    fun unselectPlace(placeId: Uuid) {
        viewModelScope.launch {
            removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId))
        }
    }
}
