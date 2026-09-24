package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.BrowserSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.BrowserSettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.BrowserSettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class BrowserSettingLocalDataSourceImpl(
    @BrowserSettingDataStore
    private val dataStore: DataStore<BrowserSettingData>,
) : BrowserSettingLocalDataSource {
    override fun getChromeSessionProfileDirectory(): Flow<String> =
        dataStore
            .data
            .map { setting -> setting.chromeSessionProfileDirectory }

    override suspend fun setChromeSessionProfileDirectory(directory: String) {
        dataStore.updateData { setting -> setting.copy(chromeSessionProfileDirectory = directory) }
    }
}
