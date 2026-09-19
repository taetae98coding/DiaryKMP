package io.github.taetae98coding.diary.core.database.api.music.datasource

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import kotlin.uuid.Uuid

public interface AccountMusicSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MusicLocalEntity>
}
