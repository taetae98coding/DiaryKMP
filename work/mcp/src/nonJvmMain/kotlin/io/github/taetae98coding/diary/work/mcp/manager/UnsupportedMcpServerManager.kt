package io.github.taetae98coding.diary.work.mcp.manager

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Inject
@ContributesBinding(AppScope::class, binding = binding<McpServerManager>())
internal class UnsupportedMcpServerManager : McpServerManager {
    override fun isSupported(): Boolean = false

    override fun getState(): Flow<McpServerState> = flowOf(McpServerState.Off)

    override suspend fun start() = Unit

    override suspend fun stop() = Unit
}
