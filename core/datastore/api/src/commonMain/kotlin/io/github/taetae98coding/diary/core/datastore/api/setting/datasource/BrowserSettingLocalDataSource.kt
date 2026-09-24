package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import kotlinx.coroutines.flow.Flow

public interface BrowserSettingLocalDataSource {
    public fun getChromeSessionProfileDirectory(): Flow<String>

    public suspend fun setChromeSessionProfileDirectory(directory: String)
}
