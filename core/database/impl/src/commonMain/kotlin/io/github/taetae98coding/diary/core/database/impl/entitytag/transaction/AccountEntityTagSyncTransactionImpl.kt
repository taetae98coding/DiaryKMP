package io.github.taetae98coding.diary.core.database.impl.entitytag.transaction

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountEntityTagSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountEntityTagSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val table = database.entityTagTable(kind = kind)

        database.clearPendingEach(entityTagList) { entityTag ->
            table.clearPending(accountId = accountId, entityTag = entityTag)
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityTagList: List<EntityTagLocalEntity>,
        cursor: Long,
    ) {
        val table = database.entityTagTable(kind = kind)

        database.upsertPulled(
            accountId = accountId,
            kind = kind.syncKind,
            cursor = cursor,
            pulledList = entityTagList,
            keyOf = { entityTag -> entityTag.entityId to entityTag.tagId },
            updatedAtOf = { entityTag -> entityTag.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                table
                    .findByEntityIdList(pulledList.map { entityTag -> entityTag.entityId }.distinct())
                    .associate { entityTag -> (entityTag.entityId to entityTag.tagId) to entityTag.updatedAt }
            },
            upsert = { upsertList -> table.upsert(entityTagList = upsertList) },
            insertIgnoreAccount = { pulledList -> table.insertIgnoreAccountSynced(accountId = accountId, entityTagList = pulledList) },
        )
    }
}
