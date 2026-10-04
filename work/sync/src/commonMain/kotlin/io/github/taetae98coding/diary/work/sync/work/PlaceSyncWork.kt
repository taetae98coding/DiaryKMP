package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.place.datasource.PlaceRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
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
        pushPending(
            pendingList = accountPlaceSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> placeRemoteDataSource.push(placeList = chunk.map { place -> place.toRemote() }) },
            clearPending = { chunk -> accountPlaceSyncTransaction.clearPending(accountId = accountId, placeList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.PLACE,
            pull = { cursor -> placeRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountPlaceSyncTransaction.upsert(
                    accountId = accountId,
                    placeList = pullList.map { pull -> pull.place.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
