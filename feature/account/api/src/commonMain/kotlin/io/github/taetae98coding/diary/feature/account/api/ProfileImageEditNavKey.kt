package io.github.taetae98coding.diary.feature.account.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object ProfileImageEditNavKey : ScreenNavKey {
    override val screenName: String get() = "ProfileImageEdit"
}
