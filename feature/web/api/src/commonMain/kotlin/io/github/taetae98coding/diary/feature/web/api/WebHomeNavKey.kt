package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object WebHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "WebHome"
}
