package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagSyncTransaction
import io.github.taetae98coding.diary.core.mapper.tag.toLocal
import io.github.taetae98coding.diary.core.mapper.tag.toRemote
import io.github.taetae98coding.diary.core.network.api.tag.datasource.TagRemoteDataSource
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
        val tagList = accountTagSyncLocalDataSource.findPending(accountId = accountId)

        tagList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            tagRemoteDataSource.push(tagList = chunk.map { tag -> tag.toRemote() })
            accountTagSyncTransaction.clearPending(accountId = accountId, tagList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.TAG,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = tagRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountTagSyncTransaction.save(
                    accountId = accountId,
                    tagList = pullList.map { pull -> pull.tag.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
