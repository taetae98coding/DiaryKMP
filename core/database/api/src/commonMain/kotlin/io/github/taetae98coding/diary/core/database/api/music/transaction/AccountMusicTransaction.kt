package io.github.taetae98coding.diary.core.database.api.music.transaction

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import kotlin.uuid.Uuid

public interface AccountMusicTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
    )
}
