package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.HolidaySettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class HolidaySettingLocalDataSourceImpl(
    @HolidaySettingDataStore
    private val dataStore: DataStore<HolidaySettingData>,
) : HolidaySettingLocalDataSource {
    override fun getHiddenKeySet(): Flow<Set<String>> =
        dataStore
            .data
            .map { setting -> setting.hiddenKeySet }

    override suspend fun addHiddenKey(key: String) {
        dataStore.updateData { setting ->
            setting.withHiddenKeySet(setting.hiddenKeySet + key)
        }
    }

    override suspend fun removeHiddenKey(key: String) {
        dataStore.updateData { setting ->
            setting.withHiddenKeySet(setting.hiddenKeySet - key)
        }
    }

    override suspend fun submitHiddenKeySet(hiddenKeySet: Set<String>) {
        dataStore.updateData { setting ->
            setting.withHiddenKeySet(hiddenKeySet)
        }
    }
}

private fun HolidaySettingData.withHiddenKeySet(hiddenKeySet: Set<String>): HolidaySettingData =
    if (hiddenKeySet == this.hiddenKeySet) {
        this
    } else {
        copy(hiddenKeySet = hiddenKeySet)
    }
