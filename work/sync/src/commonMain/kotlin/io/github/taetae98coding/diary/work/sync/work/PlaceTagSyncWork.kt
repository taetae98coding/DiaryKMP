package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.placetag.datasource.PlaceTagRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class PlaceTagSyncWork(
    private val accountPlaceTagSyncLocalDataSource: AccountPlaceTagSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountPlaceTagSyncTransaction: AccountPlaceTagSyncTransaction,
    private val placeTagRemoteDataSource: PlaceTagRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        val placeTagList = accountPlaceTagSyncLocalDataSource.findPending(accountId = accountId)

        placeTagList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            placeTagRemoteDataSource.push(placeTagList = chunk.map { placeTag -> placeTag.toRemote() })
            accountPlaceTagSyncTransaction.clearPending(accountId = accountId, placeTagList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.PLACE_TAG,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = placeTagRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountPlaceTagSyncTransaction.save(
                    accountId = accountId,
                    placeTagList = pullList.map { pull -> pull.placeTag.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
