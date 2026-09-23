package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object DDayHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "DDayHome"
}
