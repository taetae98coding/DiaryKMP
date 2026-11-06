@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.qr.QrListEffect
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageTagQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
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
internal class TagDetailQrViewModel(
    @InjectedParam tagId: Uuid,
    pageQrUseCase: PageTagQrUseCase,
    private val deleteQrUseCase: DeleteQrUseCase,
    private val restoreQrUseCase: RestoreQrUseCase,
) : ViewModel() {
    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val qrPagingData: Flow<PagingData<Qr>> =
        scopeUiState
            .flatMapLatest { (scopeValue) ->
                pageQrUseCase(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = scopeValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<QrListEffect>(Channel.BUFFERED)
    val effect: Flow<QrListEffect> = _effect.receiveAsFlow()

    private val inProgressDeleteIdSet = mutableSetOf<Uuid>()
    private val inProgressRestoreIdSet = mutableSetOf<Uuid>()

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        if (!inProgressDeleteIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteQrUseCase(parameter = id)
                    .onSuccess { _effect.send(QrListEffect.Deleted(id = id)) }
            } finally {
                inProgressDeleteIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressRestoreIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreQrUseCase(parameter = id)
            } finally {
                inProgressRestoreIdSet.remove(id)
            }
        }
    }
}
