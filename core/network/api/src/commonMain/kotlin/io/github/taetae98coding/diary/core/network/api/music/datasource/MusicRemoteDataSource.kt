package io.github.taetae98coding.diary.core.network.api.music.datasource

import io.github.taetae98coding.diary.core.network.api.music.entity.MusicPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity

public interface MusicRemoteDataSource {
    public suspend fun push(musicList: List<MusicRemoteEntity>)

    public suspend fun pull(usn: Long): List<MusicPullRemoteEntity>
}
