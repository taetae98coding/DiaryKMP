package io.github.taetae98coding.diary.data.setting.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.mapper.map.toDomain
import io.github.taetae98coding.diary.core.mapper.map.toLocal
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.setting.repository.MapSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class MapSettingRepositoryImpl(
    private val mapSettingLocalDataSource: MapSettingLocalDataSource,
) : MapSettingRepository {
    override fun getDefaultProvider(): Flow<MapProvider> =
        mapSettingLocalDataSource
            .getDefaultProvider()
            .map { local -> local?.toDomain() ?: MapProvider.DEFAULT }

    override suspend fun setDefaultProvider(provider: MapProvider) {
        mapSettingLocalDataSource.setDefaultProvider(provider = provider.toLocal())
    }
}
