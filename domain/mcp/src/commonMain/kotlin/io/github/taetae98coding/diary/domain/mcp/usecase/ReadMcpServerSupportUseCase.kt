package io.github.taetae98coding.diary.domain.mcp.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager

@Inject
public class ReadMcpServerSupportUseCase internal constructor(
    private val mcpServerManager: McpServerManager,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean = mcpServerManager.isSupported()
}
