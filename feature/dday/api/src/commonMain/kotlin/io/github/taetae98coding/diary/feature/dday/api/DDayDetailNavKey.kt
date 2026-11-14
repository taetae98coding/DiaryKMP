package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class DDayDetailNavKey(
    val id: Uuid,
    val isStandalone: Boolean = false,
) : ScreenNavKey {
    override val screenName: String get() = "DDayDetail"
}
