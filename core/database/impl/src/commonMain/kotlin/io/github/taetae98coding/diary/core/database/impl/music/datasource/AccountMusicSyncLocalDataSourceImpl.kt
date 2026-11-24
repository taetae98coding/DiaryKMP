package io.github.taetae98coding.diary.core.database.impl.music.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMusicSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMusicSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MusicLocalEntity> = database.accountMusicSyncDao().findPending(accountId = accountId)
}
