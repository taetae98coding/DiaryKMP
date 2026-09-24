@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountPlaceMemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PagePlaceMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceMemoRepository: AccountPlaceMemoRepository,
) : FlowUseCase<PagePlaceMemoUseCase.Parameter, PagingData<Memo>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Memo>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountPlaceMemoRepository
                        .page(
                            account = account,
                            placeId = parameter.placeId,
                            sort = parameter.sort,
                        ).map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }

    public data class Parameter(
        val placeId: Uuid,
        val sort: ListSort,
    )
}
