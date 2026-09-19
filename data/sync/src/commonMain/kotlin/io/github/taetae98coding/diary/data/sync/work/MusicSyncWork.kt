package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.mapper.playlist.toLocal
import io.github.taetae98coding.diary.core.mapper.playlist.toRemote
import io.github.taetae98coding.diary.core.network.api.music.datasource.MusicRemoteDataSource
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
        val musicList = accountMusicSyncLocalDataSource.findPending(accountId = accountId)

        musicList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            musicRemoteDataSource.push(musicList = chunk.map { music -> music.toRemote() })
            accountMusicSyncTransaction.clearPending(accountId = accountId, musicList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MUSIC,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = musicRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMusicSyncTransaction.save(
                    accountId = accountId,
                    musicList = pullList.map { pull -> pull.music.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
