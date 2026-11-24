@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
internal class MemoPlaceViewModel(
    @Assisted private val id: Uuid,
    pagePlaceUseCase: PagePlaceUseCase,
    getMemoPlaceUseCase: GetMemoPlaceUseCase,
    private val addMemoPlaceUseCase: AddMemoPlaceUseCase,
    private val removeMemoPlaceUseCase: RemoveMemoPlaceUseCase,
) : ViewModel(),
    MemoPlaceInputController {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(id: Uuid): MemoPlaceViewModel
    }

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

    private val selectablePaging =
        MemoSelectablePaging { query -> pagePlaceUseCase(parameter = PagePlaceUseCase.Parameter(query = query, sort = ListSort.DEFAULT)) }

    val placePagingData: Flow<PagingData<Place>> = selectablePaging.pagingData.cachedIn(viewModelScope)

    val selectablePlacePagingData: Flow<PagingData<Place>> = selectablePaging.selectablePagingData.cachedIn(viewModelScope)

    private val inProgressSelectPlaceSet = mutableSetOf<Uuid>()
    private val inProgressUnselectPlaceSet = mutableSetOf<Uuid>()

    override fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    override fun selectPlace(placeId: Uuid) {
        if (!inProgressSelectPlaceSet.add(placeId)) return

        viewModelScope.launch {
            try {
                addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId))
            } finally {
                inProgressSelectPlaceSet.remove(placeId)
            }
        }
    }

    override fun unselectPlace(placeId: Uuid) {
        if (!inProgressUnselectPlaceSet.add(placeId)) return

        viewModelScope.launch {
            try {
                removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId))
            } finally {
                inProgressUnselectPlaceSet.remove(placeId)
            }
        }
    }
}
