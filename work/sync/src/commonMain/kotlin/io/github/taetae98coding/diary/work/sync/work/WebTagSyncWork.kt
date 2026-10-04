package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagSyncTransaction
import io.github.taetae98coding.diary.core.network.api.webtag.datasource.WebTagRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class WebTagSyncWork(
    private val accountWebTagSyncLocalDataSource: AccountWebTagSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountWebTagSyncTransaction: AccountWebTagSyncTransaction,
    private val webTagRemoteDataSource: WebTagRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountWebTagSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> webTagRemoteDataSource.push(webTagList = chunk.map { webTag -> webTag.toRemote() }) },
            clearPending = { chunk -> accountWebTagSyncTransaction.clearPending(accountId = accountId, webTagList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.WEB_TAG,
            pull = { cursor -> webTagRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountWebTagSyncTransaction.upsert(
                    accountId = accountId,
                    webTagList = pullList.map { pull -> pull.webTag.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
