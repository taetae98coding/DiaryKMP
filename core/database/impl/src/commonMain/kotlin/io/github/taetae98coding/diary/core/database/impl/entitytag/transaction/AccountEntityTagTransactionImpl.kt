package io.github.taetae98coding.diary.core.database.impl.entitytag.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountEntityTagTransactionImpl(
    private val database: DiaryDatabase,
) : AccountEntityTagTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        val table = database.entityTagTable(kind = kind)

        database.withWriteTransaction {
            if (isDeleted) {
                table.deleteLink(accountId = accountId, entityId = entityId, tagId = tagId, updatedAt = updatedAt)
            } else {
                table.restoreLink(accountId = accountId, entityId = entityId, tagId = tagId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun EntityTagTable<*, *>.restoreLink(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            updateDeleted(
                entityId = entityId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = updatedAt,
            )
        val entityTag =
            EntityTagLocalEntity(
                entityId = entityId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = updatedAt,
                createdAt = updatedAt,
            )

        if (restoredCount == 0) {
            upsert(entityTagList = listOf(entityTag))
        }

        upsertAccountPending(accountId = accountId, entityTagList = listOf(entityTag))
    }

    private suspend fun EntityTagTable<*, *>.deleteLink(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            updateDeleted(
                entityId = entityId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            markPending(accountId = accountId, entityId = entityId, tagId = tagId)
        }
    }
}
