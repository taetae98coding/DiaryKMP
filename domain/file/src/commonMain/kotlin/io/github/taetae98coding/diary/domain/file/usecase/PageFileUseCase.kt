@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.file.usecase

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Factory

private val LOADING_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.Loading,
        prepend = LoadState.NotLoading(endOfPaginationReached = false),
        append = LoadState.NotLoading(endOfPaginationReached = false),
    )

@Factory
public class PageFileUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileRepository: FileRepository,
) : FlowUseCase<Unit, PagingData<DiaryFile>>() {
    // 세션 갱신 여부만 바뀌어도 계정 값이 새로 오므로, 계정이 바뀔 때만 목록을 처음부터 다시 불러온다.
    override fun execute(parameter: Unit): Flow<Result<PagingData<DiaryFile>>> =
        getAccountUseCase(parameter = Unit)
            .map { result -> result.map { account -> account as? Account.User } }
            .distinctUntilChangedBy { result -> result.map { user -> user?.id } }
            .flatMapLatest { result ->
                result.fold(
                    onSuccess = { user ->
                        if (user == null) {
                            flowOf(Result.success(PagingData.empty()))
                        } else {
                            fileRepository
                                .page()
                                .map { pagingData -> Result.success(pagingData) }
                                .onStart { emit(Result.success(PagingData.empty(sourceLoadStates = LOADING_LOAD_STATES))) }
                        }
                    },
                    onFailure = { throwable -> flowOf(Result.failure(throwable)) },
                )
            }
}
