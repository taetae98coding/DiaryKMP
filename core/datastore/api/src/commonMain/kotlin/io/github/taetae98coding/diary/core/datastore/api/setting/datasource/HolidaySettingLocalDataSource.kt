package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import kotlinx.coroutines.flow.Flow

public interface HolidaySettingLocalDataSource {
    public fun getHiddenKeySet(): Flow<Set<String>>

    public suspend fun addHiddenKey(key: String)

    public suspend fun removeHiddenKey(key: String)

    public suspend fun submitHiddenKeySet(hiddenKeySet: Set<String>)
}
