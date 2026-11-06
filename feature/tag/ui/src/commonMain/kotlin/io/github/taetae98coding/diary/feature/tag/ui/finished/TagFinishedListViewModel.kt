@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.lifecycle.ViewModel
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
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagFinishedListViewModel(
    pageFinishedTagUseCase: PageFinishedTagUseCase,
    finishTagUseCase: FinishTagUseCase,
    restartTagUseCase: RestartTagUseCase,
    deleteTagUseCase: DeleteTagUseCase,
    restoreTagUseCase: RestoreTagUseCase,
) : ViewModel() {
    private val tagActions =
        ListItemActions(
            finishUseCase = finishTagUseCase,
            restartUseCase = restartTagUseCase,
            deleteUseCase = deleteTagUseCase,
            restoreUseCase = restoreTagUseCase,
            restartedEffect = TagListEffect::Restarted,
            deletedEffect = TagListEffect::Deleted,
        )

    val effect: Flow<TagListEffect> = tagActions.effect

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

    fun finish(id: Uuid) {
        viewModelScope.launch { tagActions.finish(id = id) }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch { tagActions.restart(id = id) }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { tagActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { tagActions.restore(id = id) }
    }
}
