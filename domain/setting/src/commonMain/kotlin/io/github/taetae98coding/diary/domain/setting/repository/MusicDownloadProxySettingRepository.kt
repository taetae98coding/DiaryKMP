package io.github.taetae98coding.diary.domain.setting.repository

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import kotlinx.coroutines.flow.Flow

public interface MusicDownloadProxySettingRepository {
    public fun get(): Flow<MusicDownloadProxySetting>

    public suspend fun upsert(setting: MusicDownloadProxySetting)
}
