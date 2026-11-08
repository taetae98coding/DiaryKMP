@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailDDayViewModel(
    @InjectedParam tagId: Uuid,
    pageTagDDayUseCase: PageTagDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    private val restoreDDayUseCase: RestoreDDayUseCase,
) : ViewModel() {
    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val ddayPagingData: Flow<PagingData<DDay>> =
        scopeUiState
            .flatMapLatest { (scopeValue) ->
                pageTagDDayUseCase(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = scopeValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<DDayListEffect>(Channel.BUFFERED)
    val effect: Flow<DDayListEffect> = _effect.receiveAsFlow()

    private val inProgressDeleteIdSet = mutableSetOf<Uuid>()
    private val inProgressRestoreIdSet = mutableSetOf<Uuid>()

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        if (!inProgressDeleteIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteDDayUseCase(parameter = id)
                    .onSuccess { _effect.send(DDayListEffect.Deleted(id = id)) }
            } finally {
                inProgressDeleteIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressRestoreIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreDDayUseCase(parameter = id)
            } finally {
                inProgressRestoreIdSet.remove(id)
            }
        }
    }
}
