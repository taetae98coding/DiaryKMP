
package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.core.memo.MemoListViewModel
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
internal class TagMemoFinishedListViewModel(
    @InjectedParam private val tagId: Uuid,
    private val pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    findTagUseCase: FindTagUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : MemoListViewModel<TagMemoFinishedListEffect>(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
        restartedEffect = TagMemoFinishedListEffect::Restarted,
        deletedEffect = TagMemoFinishedListEffect::Deleted,
    ) {
    val uiState: StateFlow<TagMemoFinishedListUiState> =
        findTagUseCase(parameter = tagId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { tag -> TagMemoFinishedListUiState(title = tag.detail.emojiWithTitle) }
                    ?: TagMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagMemoFinishedListUiState(),
            )

    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = sort))
}
