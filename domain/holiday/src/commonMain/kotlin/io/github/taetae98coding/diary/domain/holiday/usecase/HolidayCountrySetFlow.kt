package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal fun GetHolidayCountrySettingUseCase.countrySetFlow(): Flow<Set<HolidayCountry>> =
    invoke(parameter = Unit)
        .map { result -> result.getOrThrow().countrySet }
        .distinctUntilChanged()
