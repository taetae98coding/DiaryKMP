package io.github.taetae98coding.diary.core.database.impl.routine.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.transaction.AccountRoutineTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.routineTagTable
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineTransactionImpl(
    private val database: DiaryDatabase,
) : AccountRoutineTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        routine: RoutineLocalEntity,
        routineTagList: List<EntityTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.routineDao().upsert(routine)
            database.accountRoutineDao().upsert(
                AccountRoutineLocalEntity(
                    accountId = accountId,
                    routineId = routine.id,
                    isDirty = true,
                ),
            )
            database.routineTagTable().upsertPending(accountId = accountId, entityTagList = routineTagList)
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        routineId: Uuid,
        detail: RoutineDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            routineId = routineId,
        ) {
            database.accountRoutineDao().updateDetail(
                accountId = accountId,
                routineId = routineId,
                title = detail.title,
                description = detail.description,
                color = detail.color,
                startDate = detail.startDate,
                cycle = detail.cycle,
                unit = detail.unit,
                goalAmount = detail.goalAmount,
                amountPerRecord = detail.amountPerRecord,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateFinished(
        accountId: Uuid,
        routineId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            routineId = routineId,
        ) {
            database.accountRoutineDao().updateFinished(
                accountId = accountId,
                routineId = routineId,
                isFinished = isFinished,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        routineId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            routineId = routineId,
        ) {
            database.accountRoutineDao().updateDeleted(
                accountId = accountId,
                routineId = routineId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        routineId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountRoutineDao().markPending(accountId = accountId, routineId = routineId)
            }

            updatedCount
        }
}
