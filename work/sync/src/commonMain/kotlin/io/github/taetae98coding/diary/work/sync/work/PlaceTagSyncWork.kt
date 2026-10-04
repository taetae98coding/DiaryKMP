package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
        pushPending(
            pendingList = accountPlaceTagSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> placeTagRemoteDataSource.push(placeTagList = chunk.map { placeTag -> placeTag.toRemote() }) },
            clearPending = { chunk -> accountPlaceTagSyncTransaction.clearPending(accountId = accountId, placeTagList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.PLACE_TAG,
            pull = { cursor -> placeTagRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountPlaceTagSyncTransaction.upsert(
                    accountId = accountId,
                    placeTagList = pullList.map { pull -> pull.placeTag.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
