@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
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
) : ViewModel() {
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
        MemoSelectablePaging(scope = viewModelScope) { query -> pagePlaceUseCase(parameter = query) }

    val placePagingData: Flow<PagingData<Place>> = selectablePaging.pagingData

    val selectablePlacePagingData: Flow<PagingData<Place>> = selectablePaging.selectablePagingData

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectPlace(id: Uuid) {
        selectionUiState.update { value -> value.copy(placeIdSet = value.placeIdSet + id) }
    }

    fun unselectPlace(id: Uuid) {
        selectionUiState.update { value -> value.copy(placeIdSet = value.placeIdSet - id) }
    }
}
