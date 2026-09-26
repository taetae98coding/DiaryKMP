package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class QrHomeViewModel(
    pageQrUseCase: PageQrUseCase,
    private val deleteQrUseCase: DeleteQrUseCase,
    private val restoreQrUseCase: RestoreQrUseCase,
) : ViewModel() {
    val qrPagingData: Flow<PagingData<Qr>> =
        pageQrUseCase(parameter = Unit)
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<QrHomeEffect>(Channel.BUFFERED)
    val effect: Flow<QrHomeEffect> = _effect.receiveAsFlow()

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteQrUseCase(parameter = id)
                .onSuccess { _effect.send(QrHomeEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreQrUseCase(parameter = id)
        }
    }
}
