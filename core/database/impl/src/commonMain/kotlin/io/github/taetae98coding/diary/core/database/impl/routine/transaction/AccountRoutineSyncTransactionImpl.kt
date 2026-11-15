package io.github.taetae98coding.diary.core.database.impl.routine.transaction

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.transaction.AccountRoutineSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountRoutineSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        routineList: List<RoutineLocalEntity>,
    ) {
        database.clearPendingEach(routineList) { routine ->
            database.accountRoutineSyncDao().clearPending(
                accountId = accountId,
                routineId = routine.id,
                updatedAt = routine.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        routineList: List<RoutineLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.ROUTINE,
            cursor = cursor,
            pulledList = routineList,
            keyOf = { routine -> routine.id },
            updatedAtOf = { routine -> routine.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.routineDao().findUpdatedAt(pulledList.map { routine -> routine.id }) },
            upsert = { upsertList -> database.routineDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountRoutineSyncDao().insertIgnore(
                    pulledList.map { routine ->
                        AccountRoutineLocalEntity(
                            accountId = accountId,
                            routineId = routine.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
