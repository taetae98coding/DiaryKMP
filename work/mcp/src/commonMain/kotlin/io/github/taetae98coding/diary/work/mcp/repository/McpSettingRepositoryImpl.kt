package io.github.taetae98coding.diary.work.mcp.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.McpSettingLocalDataSource
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(AppScope::class)
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
