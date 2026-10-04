package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebSyncTransaction
import io.github.taetae98coding.diary.core.network.api.web.datasource.WebRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class WebSyncWork(
    private val accountWebSyncLocalDataSource: AccountWebSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountWebSyncTransaction: AccountWebSyncTransaction,
    private val webRemoteDataSource: WebRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountWebSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> webRemoteDataSource.push(webList = chunk.map { web -> web.toRemote() }) },
            clearPending = { chunk -> accountWebSyncTransaction.clearPending(accountId = accountId, webList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.WEB,
            pull = { cursor -> webRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountWebSyncTransaction.upsert(
                    accountId = accountId,
                    webList = pullList.map { pull -> pull.web.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
