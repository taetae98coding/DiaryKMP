package io.github.taetae98coding.diary.data.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountTagRoutineLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.routine.repository.AccountTagRoutineRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagRoutineRepositoryImpl(
    private val accountTagRoutineLocalDataSource: AccountTagRoutineLocalDataSource,
) : AccountTagRoutineRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagRoutineLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    currentCycle = currentCycle.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun pageFinished(
        account: Account,
        tagId: Uuid,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagRoutineLocalDataSource.pageFinished(
                    accountId = account.id,
                    tagId = tagId,
                    currentCycle = currentCycle.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
