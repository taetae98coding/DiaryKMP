package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import io.github.taetae98coding.diary.feature.core.paging.LOADED_EMPTY_LOAD_STATES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class DDayHomeViewModel(
    pageDDayUseCase: PageDDayUseCase,
    deleteDDayUseCase: DeleteDDayUseCase,
    restoreDDayUseCase: RestoreDDayUseCase,
) : ViewModel() {
    val ddayPagingData: Flow<PagingData<DDay>> =
        pageDDayUseCase()
            // 조건 없는 PagingData.empty()는 불러오기 상태를 싣지 않아 목록이 처음의 불러오는 중에 머물러 빈 상태로 바뀌지 않는다.
            .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
            .cachedIn(viewModelScope)

    private val ddayActions =
        ListItemDeleteActions<DDayListEffect>(
            deleteUseCase = deleteDDayUseCase,
            restoreUseCase = restoreDDayUseCase,
            deletedEffect = DDayListEffect::Deleted,
        )

    val effect: Flow<DDayListEffect> = ddayActions.effect

    fun delete(id: Uuid) {
        viewModelScope.launch { ddayActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { ddayActions.restore(id = id) }
    }
}
