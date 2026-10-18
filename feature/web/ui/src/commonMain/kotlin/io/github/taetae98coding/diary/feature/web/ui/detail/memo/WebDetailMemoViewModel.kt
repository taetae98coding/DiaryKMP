package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.memo.MemoListViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class WebDetailMemoViewModel(
    @InjectedParam private val webId: Uuid,
    private val pageWebMemoUseCase: PageWebMemoUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : MemoListViewModel<MemoListEffect>(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
        finishedEffect = MemoListEffect::Finished,
        deletedEffect = MemoListEffect::Deleted,
    ) {
    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageWebMemoUseCase(parameter = PageWebMemoUseCase.Parameter(webId = webId, sort = sort))
}
