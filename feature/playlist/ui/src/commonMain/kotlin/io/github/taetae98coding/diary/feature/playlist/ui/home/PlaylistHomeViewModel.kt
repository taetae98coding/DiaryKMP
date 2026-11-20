@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
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
internal class PlaylistHomeViewModel(
    pageMusicUseCase: PageMusicUseCase,
    deleteMusicUseCase: DeleteMusicUseCase,
    restoreMusicUseCase: RestoreMusicUseCase,
) : ViewModel() {
    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))

    val musicPagingData: Flow<PagingData<Music>> =
        sortUiState
            .flatMapLatest { (sort) -> pageMusicUseCase(parameter = sort) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val musicActions =
        ListItemDeleteActions<MusicListEffect>(
            deleteUseCase = deleteMusicUseCase,
            restoreUseCase = restoreMusicUseCase,
            deletedEffect = MusicListEffect::Deleted,
        )

    val effect: Flow<MusicListEffect> = musicActions.effect

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { musicActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { musicActions.restore(id = id) }
    }
}
