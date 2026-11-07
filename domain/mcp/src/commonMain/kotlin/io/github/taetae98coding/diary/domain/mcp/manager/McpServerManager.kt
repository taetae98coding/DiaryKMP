package io.github.taetae98coding.diary.domain.mcp.manager

import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import kotlinx.coroutines.flow.Flow

public interface McpServerManager {
    public fun isSupported(): Boolean

    public fun getState(): Flow<McpServerState>

    public suspend fun start()

    public suspend fun stop()
}
