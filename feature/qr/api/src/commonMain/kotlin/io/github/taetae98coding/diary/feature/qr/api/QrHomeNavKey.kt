package io.github.taetae98coding.diary.feature.qr.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object QrHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "QrHome"
}
