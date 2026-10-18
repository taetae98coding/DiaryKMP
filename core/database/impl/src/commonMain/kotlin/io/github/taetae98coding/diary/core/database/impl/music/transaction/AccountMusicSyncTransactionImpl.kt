package io.github.taetae98coding.diary.core.database.impl.music.transaction

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(musicList) { music ->
            database.accountMusicSyncDao().clearPending(
                accountId = accountId,
                musicId = music.id,
                updatedAt = music.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MUSIC,
            cursor = cursor,
            pulledList = musicList,
            keyOf = { music -> music.id },
            updatedAtOf = { music -> music.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.musicDao().findUpdatedAt(pulledList.map { music -> music.id }) },
            upsert = { upsertList -> database.musicDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMusicSyncDao().insertIgnore(
                    pulledList.map { music ->
                        AccountMusicLocalEntity(
                            accountId = accountId,
                            musicId = music.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
