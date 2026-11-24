package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class ChecklistHomeViewModel(
    pageChecklistUseCase: PageChecklistUseCase,
    deleteChecklistUseCase: DeleteChecklistUseCase,
    restoreChecklistUseCase: RestoreChecklistUseCase,
) : ViewModel() {
    val checklistPagingData: Flow<PagingData<ChecklistSummary>> =
        pageChecklistUseCase()
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val checklistActions =
        ListItemDeleteActions<ChecklistListEffect>(
            deleteUseCase = deleteChecklistUseCase,
            restoreUseCase = restoreChecklistUseCase,
            deletedEffect = ChecklistListEffect::Deleted,
        )

    val effect: Flow<ChecklistListEffect> = checklistActions.effect

    fun delete(id: Uuid) {
        viewModelScope.launch { checklistActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { checklistActions.restore(id = id) }
    }
}
