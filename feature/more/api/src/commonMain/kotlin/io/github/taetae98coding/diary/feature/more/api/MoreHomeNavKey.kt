package io.github.taetae98coding.diary.feature.more.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MoreHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "MoreHome"
}
