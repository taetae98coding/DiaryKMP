@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.detail.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.toMemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ContactDetailMemoViewModel(
    @InjectedParam contactId: Uuid,
    pageContactMemoUseCase: PageContactMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.DEFAULT)

    val memoPagingData: Flow<PagingData<MemoListItem>> =
        sort
            .flatMapLatest { sortValue ->
                pageContactMemoUseCase(parameter = PageContactMemoUseCase.Parameter(contactId = contactId, sort = sortValue))
                    .mapNotNull { result -> result.getOrNull() }
                    .map { pagingData -> pagingData.toMemoListItem(sort = sortValue) }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<MemoListEffect>(Channel.BUFFERED)
    val effect: Flow<MemoListEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            finishMemoUseCase(parameter = id)
                .onSuccess { _effect.send(MemoListEffect.Finished(id = id)) }
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            restartMemoUseCase(parameter = id)
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteMemoUseCase(parameter = id)
                .onSuccess { _effect.send(MemoListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreMemoUseCase(parameter = id)
        }
    }
}
