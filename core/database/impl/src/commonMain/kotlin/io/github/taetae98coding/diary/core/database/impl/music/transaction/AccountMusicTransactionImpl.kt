package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import org.koin.core.annotation.Factory
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
}
