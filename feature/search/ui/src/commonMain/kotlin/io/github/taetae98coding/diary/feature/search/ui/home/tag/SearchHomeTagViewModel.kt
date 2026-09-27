package io.github.taetae98coding.diary.feature.search.ui.home.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeTagViewModel(
    searchTagUseCase: SearchTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) : ViewModel() {
    private val query = SearchHomeQueryInput(scope = viewModelScope)

    val appliedQuery: StateFlow<String> = query.appliedQuery

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val pagingData: Flow<PagingData<Tag>> =
        query.pagingData(sort = sort) { value, sortValue ->
            searchTagUseCase(parameter = SearchTagUseCase.Parameter(query = value, sort = sortValue))
                .map { result -> result.getOrElse { PagingData.empty() } }
        }

    private val _effect = Channel<TagListEffect>(Channel.BUFFERED)
    val effect: Flow<TagListEffect> = _effect.receiveAsFlow()

    fun showQuery(query: String) {
        this.query.show(query = query)
    }

    fun updateQuery(query: String) {
        this.query.update(query = query)
    }

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
                .onSuccess { _effect.send(TagListEffect.Restarted(id = id)) }
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Deleted(id = id)) }
        }
    }

    fun undo(effect: TagListEffect) {
        viewModelScope.launch {
            when (effect) {
                is TagListEffect.Finished -> restartTagUseCase(parameter = effect.id)
                is TagListEffect.Restarted -> finishTagUseCase(parameter = effect.id)
                is TagListEffect.Deleted -> restoreTagUseCase(parameter = effect.id)
            }
        }
    }
}
