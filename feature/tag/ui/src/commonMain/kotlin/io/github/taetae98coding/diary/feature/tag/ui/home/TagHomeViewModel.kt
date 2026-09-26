@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.lifecycle.ViewModel
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
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagHomeViewModel(
    getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase,
    pageTagHomeUseCase: PageTagHomeUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) : ViewModel() {
    val filterUiState: StateFlow<TagHomeScaffoldFilterUiState> =
        getTopLevelTagFilterUseCase(parameter = Unit)
            .map { result -> TagHomeScaffoldFilterUiState(isApplied = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagHomeScaffoldFilterUiState(),
            )

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val tagPagingData: Flow<PagingData<Tag>> =
        sort
            .flatMapLatest { value ->
                pageTagHomeUseCase(parameter = value)
                    .runningFold<Result<PagingData<Tag>>, PagingData<Tag>?>(initial = null) { last, result ->
                        result.getOrElse { last ?: PagingData.empty() }
                    }.filterNotNull()
                    .distinctUntilChanged()
            }.cachedIn(viewModelScope)

    private val _effect = Channel<TagListEffect>(Channel.BUFFERED)
    val effect: Flow<TagListEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            finishTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Finished(id = id)) }
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            restartTagUseCase(parameter = id)
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
