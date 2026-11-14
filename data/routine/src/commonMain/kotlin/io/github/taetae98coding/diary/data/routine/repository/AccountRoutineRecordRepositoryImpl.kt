package io.github.taetae98coding.diary.data.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.routinerecord.datasource.AccountRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.transaction.AccountRoutineRecordTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.data.routine.mapper.toLocal
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineRecordRepositoryImpl(
    private val accountRoutineRecordLocalDataSource: AccountRoutineRecordLocalDataSource,
    private val accountRoutineRecordTransaction: AccountRoutineRecordTransaction,
) : AccountRoutineRecordRepository {
    override fun page(
        account: Account,
        routineId: Uuid,
    ): Flow<PagingData<RoutineRecord>> =
        pagingFlow(
            pagingSourceFactory = {
                accountRoutineRecordLocalDataSource.page(
                    accountId = account.id,
                    routineId = routineId,
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun find(
        account: Account,
        recordId: Uuid,
    ): Flow<RoutineRecord?> =
        accountRoutineRecordLocalDataSource
            .find(
                accountId = account.id,
                recordId = recordId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        record: RoutineRecord,
    ): Int =
        accountRoutineRecordTransaction.upsert(
            accountId = account.id,
            record = record.toLocal(),
        )

    override suspend fun updateDeleted(
        account: Account,
        recordId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountRoutineRecordTransaction.updateDeleted(
            accountId = account.id,
            recordId = recordId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
