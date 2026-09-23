package io.github.taetae98coding.diary.core.database.api.music.transaction

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMusicTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        musicList: List<MusicLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        musicId: Uuid,
        detail: MusicDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        musicId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
