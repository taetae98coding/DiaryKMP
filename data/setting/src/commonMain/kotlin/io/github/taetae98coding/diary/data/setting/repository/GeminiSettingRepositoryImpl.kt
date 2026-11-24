package io.github.taetae98coding.diary.data.setting.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.data.setting.mapper.toDomain
import io.github.taetae98coding.diary.data.setting.mapper.toLocal
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(AppScope::class)
internal class GeminiSettingRepositoryImpl(
    private val geminiSettingLocalDataSource: GeminiSettingLocalDataSource,
) : GeminiSettingRepository {
    override fun get(): Flow<GeminiSetting> =
        geminiSettingLocalDataSource
            .get()
            .map { local -> local.toDomain() }

    override suspend fun upsert(setting: GeminiSetting) {
        geminiSettingLocalDataSource.upsert(setting = setting.toLocal())
    }
}
