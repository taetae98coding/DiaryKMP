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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagFinishedListViewModel(
    pageFinishedTagUseCase: PageFinishedTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) : ViewModel() {
    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val tagPagingData: Flow<PagingData<Tag>> =
        sort
            .flatMapLatest { value -> pageFinishedTagUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<TagListEffect>(Channel.BUFFERED)
    val effect: Flow<TagListEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            finishTagUseCase(parameter = id)
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            restartTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Restarted(id = id)) }
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreTagUseCase(parameter = id)
        }
    }
}
