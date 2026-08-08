package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class HolidaySettingRepositoryImpl(
    private val holidaySettingLocalDataSource: HolidaySettingLocalDataSource,
) : HolidaySettingRepository {
    override fun getHiddenKeySet(): Flow<Set<String>> = holidaySettingLocalDataSource.getHiddenKeySet()

    override suspend fun addHiddenKey(key: String) {
        holidaySettingLocalDataSource.addHiddenKey(key = key)
    }

    override suspend fun removeHiddenKey(key: String) {
        holidaySettingLocalDataSource.removeHiddenKey(key = key)
    }

    override suspend fun submitHiddenKeySet(hiddenKeySet: Set<String>) {
        holidaySettingLocalDataSource.submitHiddenKeySet(hiddenKeySet = hiddenKeySet)
    }
}
