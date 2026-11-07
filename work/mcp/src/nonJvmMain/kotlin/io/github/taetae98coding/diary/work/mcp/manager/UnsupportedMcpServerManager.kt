package io.github.taetae98coding.diary.work.mcp.manager

import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Factory

@Factory(binds = [McpServerManager::class])
internal class UnsupportedMcpServerManager : McpServerManager {
    override fun isSupported(): Boolean = false

    override fun getState(): Flow<McpServerState> = flowOf(McpServerState.Off)

    override suspend fun start() = Unit

    override suspend fun stop() = Unit
}
