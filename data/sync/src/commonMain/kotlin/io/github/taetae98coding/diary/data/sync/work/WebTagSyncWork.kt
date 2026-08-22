package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagSyncTransaction
import io.github.taetae98coding.diary.core.mapper.webtag.toLocal
import io.github.taetae98coding.diary.core.mapper.webtag.toRemote
import io.github.taetae98coding.diary.core.network.api.webtag.datasource.WebTagRemoteDataSource
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
        val webTagList = accountWebTagSyncLocalDataSource.findPending(accountId = accountId)

        webTagList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            webTagRemoteDataSource.push(webTagList = chunk.map { webTag -> webTag.toRemote() })
            accountWebTagSyncTransaction.clearPending(accountId = accountId, webTagList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.WEB_TAG,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = webTagRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountWebTagSyncTransaction.save(
                    accountId = accountId,
                    webTagList = pullList.map { pull -> pull.webTag.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
