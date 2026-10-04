package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.music.datasource.MusicRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MusicSyncWork(
    private val accountMusicSyncLocalDataSource: AccountMusicSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMusicSyncTransaction: AccountMusicSyncTransaction,
    private val musicRemoteDataSource: MusicRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountMusicSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> musicRemoteDataSource.push(musicList = chunk.map { music -> music.toRemote() }) },
            clearPending = { chunk -> accountMusicSyncTransaction.clearPending(accountId = accountId, musicList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MUSIC,
            pull = { cursor -> musicRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMusicSyncTransaction.upsert(
                    accountId = accountId,
                    musicList = pullList.map { pull -> pull.music.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
