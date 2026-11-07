package io.github.taetae98coding.diary.domain.mcp.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMcpServerEnabledUseCase internal constructor(
    private val mcpSettingRepository: McpSettingRepository,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> =
        mcpSettingRepository
            .getEnabled()
            .map { isEnabled -> Result.success(isEnabled) }
}
