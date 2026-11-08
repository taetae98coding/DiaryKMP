package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.transaction.AccountDDayTransaction
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.ddayTagTable
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountDDayTransactionImpl(
    private val database: DiaryDatabase,
) : AccountDDayTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        dday: DDayLocalEntity,
        ddayTagList: List<EntityTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.ddayDao().upsert(dday)
            database.accountDDayDao().upsert(
                AccountDDayLocalEntity(
                    accountId = accountId,
                    ddayId = dday.id,
                    isDirty = true,
                ),
            )
            database.ddayTagTable().upsertPending(accountId = accountId, entityTagList = ddayTagList)
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        ddayId: Uuid,
        detail: DDayDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            ddayId = ddayId,
        ) {
            database.accountDDayDao().updateDetail(
                accountId = accountId,
                ddayId = ddayId,
                title = detail.title,
                description = detail.description,
                color = detail.color,
                baseDate = detail.baseDate,
                countsBaseDay = detail.countsBaseDay,
                repeats = detail.repeats,
                endDate = detail.endDate,
                overflowPolicy = detail.overflowPolicy,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        ddayId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            ddayId = ddayId,
        ) {
            database.accountDDayDao().updateDeleted(
                accountId = accountId,
                ddayId = ddayId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        ddayId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountDDayDao().markPending(accountId = accountId, ddayId = ddayId)
            }

            updatedCount
        }
}
