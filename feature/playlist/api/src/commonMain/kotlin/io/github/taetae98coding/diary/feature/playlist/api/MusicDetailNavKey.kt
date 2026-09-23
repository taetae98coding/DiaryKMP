package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class MusicDetailNavKey(
    val id: Uuid,
) : ScreenNavKey {
    override val screenName: String get() = "MusicDetail"
}
