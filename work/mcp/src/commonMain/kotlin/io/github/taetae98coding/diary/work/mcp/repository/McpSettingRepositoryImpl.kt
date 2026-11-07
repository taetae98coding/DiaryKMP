package io.github.taetae98coding.diary.work.mcp.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.McpSettingLocalDataSource
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class McpSettingRepositoryImpl(
    private val mcpSettingLocalDataSource: McpSettingLocalDataSource,
) : McpSettingRepository {
    override fun getEnabled(): Flow<Boolean> =
        mcpSettingLocalDataSource
            .getEnabled()
            .map { isEnabled -> isEnabled ?: false }

    override suspend fun setEnabled(isEnabled: Boolean) {
        mcpSettingLocalDataSource.upsertEnabled(isEnabled = isEnabled)
    }
}
