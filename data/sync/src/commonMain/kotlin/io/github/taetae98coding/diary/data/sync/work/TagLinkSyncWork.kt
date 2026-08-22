package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkSyncTransaction
import io.github.taetae98coding.diary.core.mapper.taglink.toLocal
import io.github.taetae98coding.diary.core.mapper.taglink.toRemote
import io.github.taetae98coding.diary.core.network.api.taglink.datasource.TagLinkRemoteDataSource
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
        val tagLinkList = accountTagLinkSyncLocalDataSource.findPending(accountId = accountId)

        tagLinkList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            tagLinkRemoteDataSource.push(tagLinkList = chunk.map { tagLink -> tagLink.toRemote() })
            accountTagLinkSyncTransaction.clearPending(accountId = accountId, tagLinkList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.TAG_LINK,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = tagLinkRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountTagLinkSyncTransaction.save(
                    accountId = accountId,
                    tagLinkList = pullList.map { pull -> pull.tagLink.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
