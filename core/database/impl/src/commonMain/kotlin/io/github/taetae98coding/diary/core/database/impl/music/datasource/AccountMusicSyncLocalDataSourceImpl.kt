package io.github.taetae98coding.diary.core.database.impl.music.datasource

import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMusicSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMusicSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MusicLocalEntity> = database.accountMusicSyncDao().findPending(accountId = accountId)
}
