package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class WebDetailNavKey(
    val id: Uuid,
) : ScreenNavKey {
    override val screenName: String get() = "WebDetail"
}
