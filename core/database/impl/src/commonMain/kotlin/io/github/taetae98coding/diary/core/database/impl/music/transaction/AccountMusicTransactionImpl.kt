package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMusicTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMusicTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.musicDao().upsert(musicList)
            database.accountMusicDao().upsert(
                musicList.map { music ->
                    AccountMusicLocalEntity(
                        accountId = accountId,
                        musicId = music.id,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        musicId: Uuid,
        detail: MusicDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            musicId = musicId,
        ) {
            database.accountMusicDao().updateDetail(
                accountId = accountId,
                musicId = musicId,
                link = detail.link,
                title = detail.title,
                artist = detail.artist,
                thumbnail = detail.thumbnail,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        musicId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            musicId = musicId,
        ) {
            database.accountMusicDao().updateDeleted(
                accountId = accountId,
                musicId = musicId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        musicId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountMusicDao().markPending(accountId = accountId, musicId = musicId)
            }

            updatedCount
        }
}
