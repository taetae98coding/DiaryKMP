@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageTagMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailMusicViewModel(
    @InjectedParam tagId: Uuid,
    pageMusicUseCase: PageTagMusicUseCase,
    private val deleteMusicUseCase: DeleteMusicUseCase,
    private val restoreMusicUseCase: RestoreMusicUseCase,
) : ViewModel() {
    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))

    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val musicPagingData: Flow<PagingData<Music>> =
        combine(sortUiState, scopeUiState) { (sortValue), (scopeValue) -> sortValue to scopeValue }
            .flatMapLatest { (sortValue, scopeValue) ->
                pageMusicUseCase(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = scopeValue, sort = sortValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<MusicListEffect>(Channel.BUFFERED)
    val effect: Flow<MusicListEffect> = _effect.receiveAsFlow()

    private val inProgressDeleteIdSet = mutableSetOf<Uuid>()
    private val inProgressRestoreIdSet = mutableSetOf<Uuid>()

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        if (!inProgressDeleteIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteMusicUseCase(parameter = id)
                    .onSuccess { _effect.send(MusicListEffect.Deleted(id = id)) }
            } finally {
                inProgressDeleteIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressRestoreIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreMusicUseCase(parameter = id)
            } finally {
                inProgressRestoreIdSet.remove(id)
            }
        }
    }
}
