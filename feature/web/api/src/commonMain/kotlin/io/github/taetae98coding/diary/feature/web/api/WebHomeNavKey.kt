package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object WebHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "WebHome"
}
