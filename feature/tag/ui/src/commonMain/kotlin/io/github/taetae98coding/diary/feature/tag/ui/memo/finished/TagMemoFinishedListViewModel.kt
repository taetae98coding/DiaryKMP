@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.toMemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagMemoFinishedListViewModel(
    @InjectedParam tagId: Uuid,
    pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    findTagUseCase: FindTagUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    val uiState: StateFlow<TagMemoFinishedListUiState> =
        findTagUseCase(parameter = tagId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { tag -> TagMemoFinishedListUiState(title = tag.detail.emojiWithTitle) }
                    ?: TagMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = TagMemoFinishedListUiState(),
            )

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.DEFAULT)

    val memoPagingData: Flow<PagingData<MemoListItem>> =
        sort
            .flatMapLatest { value ->
                pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = value))
                    .mapNotNull { result -> result.getOrNull() }
                    .map { pagingData -> pagingData.toMemoListItem(sort = value) }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<TagMemoFinishedListEffect>(Channel.BUFFERED)
    val effect: Flow<TagMemoFinishedListEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            restartMemoUseCase(parameter = id)
                .onSuccess { _effect.send(TagMemoFinishedListEffect.Restarted(id = id)) }
        }
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            finishMemoUseCase(parameter = id)
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteMemoUseCase(parameter = id)
                .onSuccess { _effect.send(TagMemoFinishedListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreMemoUseCase(parameter = id)
        }
    }
}
