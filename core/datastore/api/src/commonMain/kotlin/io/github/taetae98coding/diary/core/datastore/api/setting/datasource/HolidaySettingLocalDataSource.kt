package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import kotlinx.coroutines.flow.Flow

public interface HolidaySettingLocalDataSource {
    public fun getHiddenKeySet(): Flow<Set<String>>

    public suspend fun addHiddenKey(key: String)

    public suspend fun removeHiddenKey(key: String)

    public suspend fun upsertHiddenKeySet(hiddenKeySet: Set<String>)

    public fun getCountryOptionSet(): Flow<Set<HolidayCountryOptionLocalEntity>>

    public suspend fun addCountryOption(option: HolidayCountryOptionLocalEntity)

    public suspend fun removeCountryOption(option: HolidayCountryOptionLocalEntity)
}
