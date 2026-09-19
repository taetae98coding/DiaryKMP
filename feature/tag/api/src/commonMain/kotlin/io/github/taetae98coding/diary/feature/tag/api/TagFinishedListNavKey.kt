package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object TagFinishedListNavKey : ScreenNavKey {
    override val screenName: String get() = "TagFinishedList"
}
