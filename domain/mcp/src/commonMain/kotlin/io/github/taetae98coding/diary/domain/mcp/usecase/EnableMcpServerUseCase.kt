package io.github.taetae98coding.diary.domain.mcp.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import org.koin.core.annotation.Factory

@Factory
public class EnableMcpServerUseCase internal constructor(
    private val mcpSettingRepository: McpSettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        mcpSettingRepository.setEnabled(isEnabled = true)
    }
}
