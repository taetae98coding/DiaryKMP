package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object TagFinishedListNavKey : ScreenNavKey {
    override val screenName: String get() = "TagFinishedList"
}
