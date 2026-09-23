package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMusicSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMusicSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
    ) {
        database.withWriteTransaction {
            musicList.forEach { music ->
                database.accountMusicSyncDao().clearPending(
                    accountId = accountId,
                    musicId = music.id,
                    updatedAt = music.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.musicDao().findUpdatedAt(musicList.map { music -> music.id })
            database.musicDao().upsert(
                musicList.filter { music ->
                    val localUpdatedAt = localUpdatedAtMap[music.id]
                    localUpdatedAt == null || music.updatedAt >= localUpdatedAt
                },
            )
            database.accountMusicSyncDao().insertIgnore(
                musicList.map { music ->
                    AccountMusicLocalEntity(
                        accountId = accountId,
                        musicId = music.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MUSIC),
                    usn = cursor,
                ),
            )
        }
    }
}
