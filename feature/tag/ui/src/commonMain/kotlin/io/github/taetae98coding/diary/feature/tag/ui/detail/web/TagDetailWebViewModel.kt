@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailWebViewModel(
    @InjectedParam tagId: Uuid,
    pageTagWebUseCase: PageTagWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
) : ViewModel() {
    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val scope: StateFlow<TagScope>
        field = MutableStateFlow(TagScope.SELF)

    val webPagingData: Flow<PagingData<Web>> =
        combine(sort, scope) { sortValue, scopeValue -> sortValue to scopeValue }
            .flatMapLatest { (sortValue, scopeValue) ->
                pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = scopeValue, sort = sortValue))
                    .runningFold<Result<PagingData<Web>>, PagingData<Web>?>(initial = null) { last, result ->
                        result.getOrElse { last ?: PagingData.empty() }
                    }.filterNotNull()
                    .distinctUntilChanged()
            }.cachedIn(viewModelScope)

    private val _effect = Channel<WebListEffect>(Channel.BUFFERED)
    val effect: Flow<WebListEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun select(scope: TagScope) {
        this.scope.value = scope
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteWebUseCase(parameter = id)
                .onSuccess { _effect.send(WebListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreWebUseCase(parameter = id)
        }
    }
}
