@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class WebHomeViewModel(
    pageWebUseCase: PageWebUseCase,
) : ViewModel() {
    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val webPagingData: Flow<PagingData<Web>> =
        sort
            .flatMapLatest { value -> pageWebUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    fun select(sort: ListSort) {
        this.sort.value = sort
    }
}
