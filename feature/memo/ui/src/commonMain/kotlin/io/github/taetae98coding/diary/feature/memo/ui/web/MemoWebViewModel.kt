@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoWebViewModel(
    @InjectedParam private val id: Uuid,
    pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase,
    getMemoWebUseCase: GetMemoWebUseCase,
    private val addMemoWebUseCase: AddMemoWebUseCase,
    private val removeMemoWebUseCase: RemoveMemoWebUseCase,
) : ViewModel() {
    private val selectablePaging =
        MemoSelectablePaging(scope = viewModelScope) { query -> pageMemoSelectableWebUseCase(parameter = query) }

    val webPagingData: Flow<PagingData<Web>> = selectablePaging.pagingData

    val selectableWebPagingData: Flow<PagingData<Web>> = selectablePaging.selectablePagingData

    val uiState: StateFlow<MemoWebInputUiState> =
        getMemoWebUseCase(parameter = id)
            .map { result -> MemoWebInputUiState(selectedWebList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoWebInputUiState(),
            )

    private val inProgressSelectWebSet = mutableSetOf<Uuid>()
    private val inProgressUnselectWebSet = mutableSetOf<Uuid>()

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectWeb(webId: Uuid) {
        if (!inProgressSelectWebSet.add(webId)) return

        viewModelScope.launch {
            try {
                addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = id, webId = webId))
            } finally {
                inProgressSelectWebSet.remove(webId)
            }
        }
    }

    fun unselectWeb(webId: Uuid) {
        if (!inProgressUnselectWebSet.add(webId)) return

        viewModelScope.launch {
            try {
                removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = id, webId = webId))
            } finally {
                inProgressUnselectWebSet.remove(webId)
            }
        }
    }
}
