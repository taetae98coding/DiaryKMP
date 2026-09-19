package io.github.taetae98coding.diary.feature.calendar.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object CalendarHomeFilterNavKey : ScreenNavKey {
    override val screenName: String get() = "CalendarHomeFilter"
}
