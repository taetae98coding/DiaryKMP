@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActionViewModel
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TagHomeViewModel(
    getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase,
    pageTagHomeUseCase: PageTagHomeUseCase,
    finishTagUseCase: FinishTagUseCase,
    restartTagUseCase: RestartTagUseCase,
    deleteTagUseCase: DeleteTagUseCase,
    restoreTagUseCase: RestoreTagUseCase,
) : ListItemActionViewModel<TagListEffect>(
        finishUseCase = finishTagUseCase,
        restartUseCase = restartTagUseCase,
        deleteUseCase = deleteTagUseCase,
        restoreUseCase = restoreTagUseCase,
        finishedEffect = TagListEffect::Finished,
        deletedEffect = TagListEffect::Deleted,
    ) {
    val filterUiState: StateFlow<TagHomeScaffoldFilterUiState> =
        getTopLevelTagFilterUseCase(parameter = Unit)
            .map { result -> TagHomeScaffoldFilterUiState(isLoaded = true, isApplied = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagHomeScaffoldFilterUiState(),
            )

    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))

    val tagPagingData: Flow<PagingData<Tag>> =
        sortUiState
            .flatMapLatest { (sort) ->
                pageTagHomeUseCase(parameter = sort).map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }
}
