package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MusicDownloadProxySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.MusicDownloadProxySettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.MusicDownloadProxySettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class MusicDownloadProxySettingLocalDataSourceImpl(
    @MusicDownloadProxySettingDataStore
    private val dataStore: DataStore<MusicDownloadProxySettingData>,
) : MusicDownloadProxySettingLocalDataSource {
    override fun getAddress(): Flow<String> =
        dataStore
            .data
            .map { setting -> setting.address }

    override suspend fun upsertAddress(address: String) {
        dataStore.updateData { setting -> setting.copy(address = address) }
    }
}
