package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class HolidaySettingData(
    val hiddenKeySet: Set<String> = emptySet(),
)
