package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkSyncTransaction
import io.github.taetae98coding.diary.core.network.api.taglink.datasource.TagLinkRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class TagLinkSyncWork(
    private val accountTagLinkSyncLocalDataSource: AccountTagLinkSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountTagLinkSyncTransaction: AccountTagLinkSyncTransaction,
    private val tagLinkRemoteDataSource: TagLinkRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountTagLinkSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> tagLinkRemoteDataSource.push(tagLinkList = chunk.map { tagLink -> tagLink.toRemote() }) },
            clearPending = { chunk -> accountTagLinkSyncTransaction.clearPending(accountId = accountId, tagLinkList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.TAG_LINK,
            pull = { cursor -> tagLinkRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountTagLinkSyncTransaction.upsert(
                    accountId = accountId,
                    tagLinkList = pullList.map { pull -> pull.tagLink.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
