package io.github.taetae98coding.diary.feature.holiday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object HolidayHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "HolidayHome"
}
