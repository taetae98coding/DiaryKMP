package io.github.taetae98coding.diary.domain.mcp.repository

import kotlinx.coroutines.flow.Flow

public interface McpSettingRepository {
    public fun getEnabled(): Flow<Boolean>

    public suspend fun setEnabled(isEnabled: Boolean)
}
