package io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.transaction.AccountRoutineRecordTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineRecordTransactionImpl(
    private val database: DiaryDatabase,
) : AccountRoutineRecordTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        record: RoutineRecordLocalEntity,
    ): Int =
        database.withWriteTransaction {
            val isRoutineLinked =
                database.accountRoutineDao().exists(
                    accountId = accountId,
                    routineId = record.routineId,
                )
            if (!isRoutineLinked) return@withWriteTransaction 0

            database.routineRecordDao().upsert(record)
            database.accountRoutineRecordDao().upsert(
                AccountRoutineRecordLocalEntity(
                    accountId = accountId,
                    routineRecordId = record.id,
                    isDirty = true,
                ),
            )
            1
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        recordId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        database.withWriteTransaction {
            val updatedCount =
                database.accountRoutineRecordDao().updateDeleted(
                    accountId = accountId,
                    recordId = recordId,
                    isDeleted = isDeleted,
                    updatedAt = updatedAt,
                )
            if (updatedCount > 0) {
                database.accountRoutineRecordDao().markPending(accountId = accountId, recordId = recordId)
            }
            updatedCount
        }
}
