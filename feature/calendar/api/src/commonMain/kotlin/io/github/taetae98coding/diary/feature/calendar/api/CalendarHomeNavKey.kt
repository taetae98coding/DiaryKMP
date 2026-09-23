package io.github.taetae98coding.diary.feature.calendar.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object CalendarHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "CalendarHome"
}
