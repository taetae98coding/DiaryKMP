package io.github.taetae98coding.diary.feature.web.ui.memo.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListViewModel
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class WebMemoFinishedListViewModel(
    @InjectedParam private val webId: Uuid,
    private val pageFinishedWebMemoUseCase: PageFinishedWebMemoUseCase,
    findWebUseCase: FindWebUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : EntityMemoFinishedListViewModel(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
    ) {
    override val uiState: StateFlow<EntityMemoFinishedListUiState> =
        findWebUseCase(parameter = webId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { web -> EntityMemoFinishedListUiState(title = web.detail.title) }
                    ?: EntityMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityMemoFinishedListUiState(),
            )

    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageFinishedWebMemoUseCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = sort))
}
