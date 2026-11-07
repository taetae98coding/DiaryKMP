package io.github.taetae98coding.diary.domain.mcp.usecase

import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMcpServerStateUseCase internal constructor(
    private val mcpServerManager: McpServerManager,
) : FlowUseCase<Unit, McpServerState>() {
    override fun execute(parameter: Unit): Flow<Result<McpServerState>> =
        mcpServerManager
            .getState()
            .map { state -> Result.success(state) }
}
