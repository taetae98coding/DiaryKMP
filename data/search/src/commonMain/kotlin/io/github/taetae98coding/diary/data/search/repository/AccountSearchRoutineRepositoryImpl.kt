package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchRoutineLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchRoutineRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class AccountSearchRoutineRepositoryImpl(
    private val searchRoutineLocalDataSource: SearchRoutineLocalDataSource,
) : AccountSearchRoutineRepository {
    override fun page(
        account: Account,
        query: String,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                searchRoutineLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    currentCycle = currentCycle.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
