package io.github.taetae98coding.diary.data.setting.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MusicDownloadProxySettingLocalDataSource
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class MusicDownloadProxySettingRepositoryImpl(
    private val musicDownloadProxySettingLocalDataSource: MusicDownloadProxySettingLocalDataSource,
) : MusicDownloadProxySettingRepository {
    override fun get(): Flow<MusicDownloadProxySetting> =
        musicDownloadProxySettingLocalDataSource
            .getAddress()
            .map { address -> MusicDownloadProxySetting(address = address) }

    override suspend fun upsert(setting: MusicDownloadProxySetting) {
        musicDownloadProxySettingLocalDataSource.upsertAddress(address = setting.address)
    }
}
