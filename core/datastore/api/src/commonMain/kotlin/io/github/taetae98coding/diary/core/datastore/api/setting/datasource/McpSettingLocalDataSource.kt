package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import kotlinx.coroutines.flow.Flow

public interface McpSettingLocalDataSource {
    public fun getEnabled(): Flow<Boolean?>

    public suspend fun upsertEnabled(isEnabled: Boolean)
}
