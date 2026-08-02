@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailWebViewModel(
    @InjectedParam tagId: Uuid,
    pageTagWebUseCase: PageTagWebUseCase,
) : ViewModel() {
    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val scope: StateFlow<TagScope>
        field = MutableStateFlow(TagScope.SELF)

    val webPagingData: Flow<PagingData<Web>> =
        combine(sort, scope) { sortValue, scopeValue -> sortValue to scopeValue }
            .flatMapLatest { (sortValue, scopeValue) ->
                pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = scopeValue, sort = sortValue))
            }.mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun select(scope: TagScope) {
        this.scope.value = scope
    }
}
