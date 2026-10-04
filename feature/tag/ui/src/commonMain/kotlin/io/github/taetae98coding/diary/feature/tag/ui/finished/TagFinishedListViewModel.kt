@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActionViewModel
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TagFinishedListViewModel(
    pageFinishedTagUseCase: PageFinishedTagUseCase,
    finishTagUseCase: FinishTagUseCase,
    restartTagUseCase: RestartTagUseCase,
    deleteTagUseCase: DeleteTagUseCase,
    restoreTagUseCase: RestoreTagUseCase,
) : ListItemActionViewModel<TagListEffect>(
        finishUseCase = finishTagUseCase,
        restartUseCase = restartTagUseCase,
        deleteUseCase = deleteTagUseCase,
        restoreUseCase = restoreTagUseCase,
        restartedEffect = TagListEffect::Restarted,
        deletedEffect = TagListEffect::Deleted,
    ) {
    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))

    val tagPagingData: Flow<PagingData<Tag>> =
        sortUiState
            .flatMapLatest { (sort) ->
                pageFinishedTagUseCase(parameter = sort).map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }
}
