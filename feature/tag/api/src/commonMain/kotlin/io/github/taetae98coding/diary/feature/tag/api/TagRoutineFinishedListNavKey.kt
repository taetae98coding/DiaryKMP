package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class TagRoutineFinishedListNavKey(
    val tagId: Uuid,
) : ScreenNavKey {
    override val screenName: String get() = "TagRoutineFinishedList"
}
