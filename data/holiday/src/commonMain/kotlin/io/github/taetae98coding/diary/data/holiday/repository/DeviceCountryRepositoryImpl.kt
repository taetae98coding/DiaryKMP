package io.github.taetae98coding.diary.data.holiday.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.repository.DeviceCountryRepository
import io.github.taetae98coding.diary.library.locale.DeviceLocale

@Inject
@ContributesBinding(AppScope::class)
internal class DeviceCountryRepositoryImpl : DeviceCountryRepository {
    override suspend fun read(): HolidayCountry? =
        when (DeviceLocale.currentRegionCode()) {
            KOREA_REGION_CODE -> HolidayCountry.KOREA
            UNITED_STATES_REGION_CODE -> HolidayCountry.UNITED_STATES
            else -> null
        }

    private companion object {
        const val KOREA_REGION_CODE = "KR"
        const val UNITED_STATES_REGION_CODE = "US"
    }
}
