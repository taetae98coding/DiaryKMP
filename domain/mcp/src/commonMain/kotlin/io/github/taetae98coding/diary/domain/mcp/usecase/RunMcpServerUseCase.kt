package io.github.taetae98coding.diary.domain.mcp.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.annotation.Factory

@Factory
public class RunMcpServerUseCase internal constructor(
    private val mcpSettingRepository: McpSettingRepository,
    private val mcpServerManager: McpServerManager,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        mcpSettingRepository
            .getEnabled()
            .distinctUntilChanged()
            .collectLatest { isEnabled ->
                if (isEnabled) {
                    mcpServerManager.start()
                } else {
                    mcpServerManager.stop()
                }
            }
    }
}
