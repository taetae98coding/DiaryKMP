package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import kotlinx.coroutines.flow.Flow

public interface MusicDownloadProxySettingLocalDataSource {
    public fun getAddress(): Flow<String>

    public suspend fun upsertAddress(address: String)
}
