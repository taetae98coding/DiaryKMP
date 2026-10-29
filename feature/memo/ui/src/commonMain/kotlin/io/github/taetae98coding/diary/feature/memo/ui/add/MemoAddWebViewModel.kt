@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoAddWebViewModel(
    @InjectedParam initialWebId: Uuid?,
    pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase,
    getSelectedWebUseCase: GetSelectedWebUseCase,
) : ViewModel() {
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
        MemoSelectablePaging(scope = viewModelScope) { query -> pageMemoSelectableWebUseCase(parameter = query) }

    val webPagingData: Flow<PagingData<Web>> = selectablePaging.pagingData

    val selectableWebPagingData: Flow<PagingData<Web>> = selectablePaging.selectablePagingData

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectWeb(id: Uuid) {
        selectionUiState.update { value -> value.copy(webIdSet = value.webIdSet + id) }
    }

    fun unselectWeb(id: Uuid) {
        selectionUiState.update { value -> value.copy(webIdSet = value.webIdSet - id) }
    }
}
