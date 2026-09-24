package io.github.taetae98coding.diary.domain.holiday.repository

import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import kotlinx.coroutines.flow.Flow

public interface HolidaySettingRepository {
    public fun getHiddenKeySet(): Flow<Set<String>>

    public suspend fun addHiddenKey(key: String)

    public suspend fun removeHiddenKey(key: String)

    public suspend fun submitHiddenKeySet(hiddenKeySet: Set<String>)

    public fun getCountryOptionSet(): Flow<Set<HolidayCountryOption>>

    public suspend fun addCountryOption(option: HolidayCountryOption)

    public suspend fun removeCountryOption(option: HolidayCountryOption)
}
