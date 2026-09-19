package io.github.taetae98coding.diary.feature.login.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object LoginHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "LoginHome"
}
