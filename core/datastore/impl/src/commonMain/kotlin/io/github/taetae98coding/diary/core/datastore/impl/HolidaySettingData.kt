package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class HolidaySettingData(
    val hiddenKeySet: Set<String> = emptySet(),
    val countryOptionSet: Set<String> = setOf(HolidayCountryOptionLocalEntity.DEVICE.persistentValue),
)
