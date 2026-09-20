package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
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
        val webList = accountWebSyncLocalDataSource.findPending(accountId = accountId)

        webList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            webRemoteDataSource.push(webList = chunk.map { web -> web.toRemote() })
            accountWebSyncTransaction.clearPending(accountId = accountId, webList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.WEB,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = webRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountWebSyncTransaction.save(
                    accountId = accountId,
                    webList = pullList.map { pull -> pull.web.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
