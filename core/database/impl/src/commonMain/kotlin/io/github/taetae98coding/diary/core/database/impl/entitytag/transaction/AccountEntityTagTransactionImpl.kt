package io.github.taetae98coding.diary.core.database.impl.entitytag.transaction

import androidx.room3.withWriteTransaction
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.deleteLink
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.restoreLink
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
}
