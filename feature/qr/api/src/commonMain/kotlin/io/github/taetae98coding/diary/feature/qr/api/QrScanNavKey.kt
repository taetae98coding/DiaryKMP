package io.github.taetae98coding.diary.feature.qr.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object QrScanNavKey : ScreenNavKey {
    override val screenName: String get() = "QrScan"
}
