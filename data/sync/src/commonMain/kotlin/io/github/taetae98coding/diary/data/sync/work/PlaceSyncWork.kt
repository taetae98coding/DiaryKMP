package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.mapper.place.toLocal
import io.github.taetae98coding.diary.core.mapper.place.toRemote
import io.github.taetae98coding.diary.core.network.api.place.datasource.PlaceRemoteDataSource
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class PlaceSyncWork(
    private val accountPlaceSyncLocalDataSource: AccountPlaceSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountPlaceSyncTransaction: AccountPlaceSyncTransaction,
    private val placeRemoteDataSource: PlaceRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        val placeList = accountPlaceSyncLocalDataSource.findPending(accountId = accountId)

        placeList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            placeRemoteDataSource.push(placeList = chunk.map { place -> place.toRemote() })
            accountPlaceSyncTransaction.clearPending(accountId = accountId, placeList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.PLACE,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = placeRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountPlaceSyncTransaction.save(
                    accountId = accountId,
                    placeList = pullList.map { pull -> pull.place.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
