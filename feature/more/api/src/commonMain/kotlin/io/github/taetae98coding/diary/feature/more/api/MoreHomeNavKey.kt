package io.github.taetae98coding.diary.feature.more.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MoreHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "MoreHome"
}
