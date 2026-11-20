@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputController
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
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
internal class MemoAddPlaceViewModel(
    @InjectedParam initialPlaceId: Uuid?,
    pagePlaceUseCase: PagePlaceUseCase,
    getSelectedPlaceUseCase: GetSelectedPlaceUseCase,
) : ViewModel(),
    MemoPlaceInputController {
    val selectionUiState: StateFlow<MemoAddPlaceSelectionUiState>
        field = MutableStateFlow(MemoAddPlaceSelectionUiState(placeIdSet = setOfNotNull(initialPlaceId)))

    val uiState: StateFlow<MemoPlaceInputUiState> =
        selectionUiState
            .flatMapLatest { value -> getSelectedPlaceUseCase(parameter = value.placeIdSet) }
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

    private val selectablePaging =
        MemoSelectablePaging { query -> pagePlaceUseCase(parameter = PagePlaceUseCase.Parameter(query = query, sort = ListSort.DEFAULT)) }

    val placePagingData: Flow<PagingData<Place>> = selectablePaging.pagingData.cachedIn(viewModelScope)

    val selectablePlacePagingData: Flow<PagingData<Place>> = selectablePaging.selectablePagingData.cachedIn(viewModelScope)

    override fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    override fun selectPlace(placeId: Uuid) {
        selectionUiState.update { value -> value.copy(placeIdSet = value.placeIdSet + placeId) }
    }

    override fun unselectPlace(placeId: Uuid) {
        selectionUiState.update { value -> value.copy(placeIdSet = value.placeIdSet - placeId) }
    }
}
