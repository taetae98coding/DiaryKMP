package io.github.taetae98coding.diary.data.setting.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.mapper.gemini.toDomain
import io.github.taetae98coding.diary.core.mapper.gemini.toLocal
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
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
