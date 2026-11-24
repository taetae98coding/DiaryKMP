@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

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
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputController
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
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
import kotlin.uuid.Uuid

@AssistedInject
internal class MemoAddWebViewModel(
    @Assisted initialWebId: Uuid?,
    pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase,
    getSelectedWebUseCase: GetSelectedWebUseCase,
) : ViewModel(),
    MemoWebInputController {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(initialWebId: Uuid?): MemoAddWebViewModel
    }

    val selectionUiState: StateFlow<MemoAddWebSelectionUiState>
        field = MutableStateFlow(MemoAddWebSelectionUiState(webIdSet = setOfNotNull(initialWebId)))

    val uiState: StateFlow<MemoWebInputUiState> =
        selectionUiState
            .flatMapLatest { value -> getSelectedWebUseCase(parameter = value.webIdSet) }
            .map { result -> MemoWebInputUiState(selectedWebList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoWebInputUiState(),
            )

    private val selectablePaging =
        MemoSelectablePaging { query -> pageMemoSelectableWebUseCase(parameter = query) }

    val webPagingData: Flow<PagingData<Web>> = selectablePaging.pagingData.cachedIn(viewModelScope)

    val selectableWebPagingData: Flow<PagingData<Web>> = selectablePaging.selectablePagingData.cachedIn(viewModelScope)

    override fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    override fun selectWeb(webId: Uuid) {
        selectionUiState.update { value -> value.copy(webIdSet = value.webIdSet + webId) }
    }

    override fun unselectWeb(webId: Uuid) {
        selectionUiState.update { value -> value.copy(webIdSet = value.webIdSet - webId) }
    }
}
