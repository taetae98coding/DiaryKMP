package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.entitytag.datasource.AccountEntityTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.entitytag.datasource.EntityTagRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class EntityTagSyncWork(
    private val accountEntityTagSyncLocalDataSource: AccountEntityTagSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountEntityTagSyncTransaction: AccountEntityTagSyncTransaction,
    private val entityTagRemoteDataSource: EntityTagRemoteDataSource,
) {
    suspend fun push(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
    ) {
        pushPending(
            pendingList = accountEntityTagSyncLocalDataSource.readPendingList(accountId = accountId, kind = kind),
            push = { chunk ->
                entityTagRemoteDataSource.push(
                    kind = kind.toRemote(),
                    entityTagList = chunk.map { entityTag -> entityTag.toRemote() },
                )
            },
            clearPending = { chunk ->
                accountEntityTagSyncTransaction.clearPending(accountId = accountId, kind = kind, entityTagList = chunk)
            },
        )
    }

    suspend fun pull(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
    ) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = kind.syncKind,
            pull = { cursor -> entityTagRemoteDataSource.pull(kind = kind.toRemote(), usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountEntityTagSyncTransaction.upsert(
                    accountId = accountId,
                    kind = kind,
                    entityTagList = pullList.map { pull -> pull.entityTag.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
