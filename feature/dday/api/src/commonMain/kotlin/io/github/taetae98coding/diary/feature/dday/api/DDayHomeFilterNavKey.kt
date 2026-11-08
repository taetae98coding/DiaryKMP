package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object DDayHomeFilterNavKey : ScreenNavKey {
    override val screenName: String get() = "DDayHomeFilter"
}
