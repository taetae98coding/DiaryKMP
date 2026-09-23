package io.github.taetae98coding.diary.feature.login.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object LoginHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "LoginHome"
}
