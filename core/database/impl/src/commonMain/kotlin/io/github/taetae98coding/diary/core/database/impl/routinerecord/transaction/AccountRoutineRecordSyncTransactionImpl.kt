package io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.transaction.AccountRoutineRecordSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineRecordSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountRoutineRecordSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        recordList: List<RoutineRecordLocalEntity>,
    ) {
        database.clearPendingEach(recordList) { record ->
            database.accountRoutineRecordSyncDao().clearPending(
                accountId = accountId,
                recordId = record.id,
                updatedAt = record.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        recordList: List<RoutineRecordLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.ROUTINE_RECORD,
            cursor = cursor,
            pulledList = recordList,
            keyOf = { record -> record.id },
            updatedAtOf = { record -> record.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.routineRecordDao().findUpdatedAt(pulledList.map { record -> record.id }) },
            upsert = { upsertList -> database.routineRecordDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountRoutineRecordSyncDao().insertIgnore(
                    pulledList.map { record ->
                        AccountRoutineRecordLocalEntity(
                            accountId = accountId,
                            routineRecordId = record.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
