package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagSyncTransaction
import io.github.taetae98coding.diary.core.network.api.tag.datasource.TagRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class TagSyncWork(
    private val accountTagSyncLocalDataSource: AccountTagSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountTagSyncTransaction: AccountTagSyncTransaction,
    private val tagRemoteDataSource: TagRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountTagSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> tagRemoteDataSource.push(tagList = chunk.map { tag -> tag.toRemote() }) },
            clearPending = { chunk -> accountTagSyncTransaction.clearPending(accountId = accountId, tagList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.TAG,
            pull = { cursor -> tagRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountTagSyncTransaction.upsert(
                    accountId = accountId,
                    tagList = pullList.map { pull -> pull.tag.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
