package io.github.taetae98coding.diary.feature.contact.ui.memo.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
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
internal class ContactMemoFinishedListViewModel(
    @InjectedParam private val contactId: Uuid,
    private val pageFinishedContactMemoUseCase: PageFinishedContactMemoUseCase,
    findContactUseCase: FindContactUseCase,
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
        findContactUseCase(parameter = contactId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { contact -> EntityMemoFinishedListUiState(title = contact.detail.name) }
                    ?: EntityMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityMemoFinishedListUiState(),
            )

    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageFinishedContactMemoUseCase(parameter = PageFinishedContactMemoUseCase.Parameter(contactId = contactId, sort = sort))
}
