package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override fun getCountryOptionSet(): Flow<Set<HolidayCountryOption>> =
        holidaySettingLocalDataSource
            .getCountryOptionSet()
            .map { optionSet -> optionSet.mapTo(mutableSetOf()) { option -> option.toDomain() } }

    override suspend fun addCountryOption(option: HolidayCountryOption) {
        holidaySettingLocalDataSource.addCountryOption(option = option.toLocal())
    }

    override suspend fun removeCountryOption(option: HolidayCountryOption) {
        holidaySettingLocalDataSource.removeCountryOption(option = option.toLocal())
    }
}
