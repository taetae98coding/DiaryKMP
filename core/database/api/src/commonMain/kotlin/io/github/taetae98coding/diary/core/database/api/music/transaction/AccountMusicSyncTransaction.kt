package io.github.taetae98coding.diary.core.database.api.music.transaction

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import kotlin.uuid.Uuid

public interface AccountMusicSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
        cursor: Long,
    )
}
