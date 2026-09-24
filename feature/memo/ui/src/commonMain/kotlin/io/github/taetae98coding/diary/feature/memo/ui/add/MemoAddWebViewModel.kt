@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
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
internal class MemoAddWebViewModel(
    @InjectedParam initialWebId: Uuid?,
    pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase,
    getSelectedWebUseCase: GetSelectedWebUseCase,
) : ViewModel() {
    val webIdSet: StateFlow<Set<Uuid>>
        field = MutableStateFlow(setOfNotNull(initialWebId))

    val uiState: StateFlow<MemoWebInputUiState> =
        webIdSet
            .flatMapLatest { webIdSet -> getSelectedWebUseCase(parameter = webIdSet) }
            .map { result -> MemoWebInputUiState(selectedWebList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoWebInputUiState(),
            )

    private val query = MutableStateFlow("")

    val webPagingData: Flow<PagingData<Web>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> pageMemoSelectableWebUseCase(parameter = value) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectWeb(id: Uuid) {
        webIdSet.update { value -> value + id }
    }

    fun unselectWeb(id: Uuid) {
        webIdSet.update { value -> value - id }
    }
}
