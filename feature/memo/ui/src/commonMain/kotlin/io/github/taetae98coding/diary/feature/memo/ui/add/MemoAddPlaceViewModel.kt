@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceReportedSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoAddPlaceViewModel(
    @InjectedParam initialPlaceId: Uuid?,
    pagePlaceUseCase: PagePlaceUseCase,
    getSelectedPlaceUseCase: GetSelectedPlaceUseCase,
) : ViewModel() {
    val placeIdSet: StateFlow<Set<Uuid>>
        field = MutableStateFlow(setOfNotNull(initialPlaceId))

    val uiState: StateFlow<MemoPlaceInputUiState> =
        placeIdSet
            .flatMapLatest { placeIdSet -> getSelectedPlaceUseCase(parameter = placeIdSet) }
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

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectPlace(id: Uuid) {
        placeIdSet.update { value -> value + id }
    }

    fun unselectPlace(id: Uuid) {
        placeIdSet.update { value -> value - id }
    }
}
