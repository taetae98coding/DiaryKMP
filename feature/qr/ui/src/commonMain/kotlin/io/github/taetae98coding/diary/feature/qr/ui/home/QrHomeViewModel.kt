package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.qr.QrListEffect
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class QrHomeViewModel(
    pageQrUseCase: PageQrUseCase,
    deleteQrUseCase: DeleteQrUseCase,
    restoreQrUseCase: RestoreQrUseCase,
) : ViewModel() {
    val qrPagingData: Flow<PagingData<Qr>> =
        pageQrUseCase()
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val qrActions =
        ListItemDeleteActions<QrListEffect>(
            deleteUseCase = deleteQrUseCase,
            restoreUseCase = restoreQrUseCase,
            deletedEffect = QrListEffect::Deleted,
        )

    val effect: Flow<QrListEffect> = qrActions.effect

    fun delete(id: Uuid) {
        viewModelScope.launch { qrActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { qrActions.restore(id = id) }
    }
}
