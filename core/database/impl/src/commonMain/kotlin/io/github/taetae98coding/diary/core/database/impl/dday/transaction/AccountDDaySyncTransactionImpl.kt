package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.transaction.AccountDDaySyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDDaySyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountDDaySyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        ddayList: List<DDayLocalEntity>,
    ) {
        database.clearPendingEach(ddayList) { dday ->
            database.accountDDaySyncDao().clearPending(
                accountId = accountId,
                ddayId = dday.id,
                updatedAt = dday.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        ddayList: List<DDayLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.DDAY,
            cursor = cursor,
            pulledList = ddayList,
            keyOf = { dday -> dday.id },
            updatedAtOf = { dday -> dday.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.ddayDao().findUpdatedAt(pulledList.map { dday -> dday.id }) },
            upsert = { upsertList -> database.ddayDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountDDaySyncDao().insertIgnore(
                    pulledList.map { dday ->
                        AccountDDayLocalEntity(
                            accountId = accountId,
                            ddayId = dday.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
