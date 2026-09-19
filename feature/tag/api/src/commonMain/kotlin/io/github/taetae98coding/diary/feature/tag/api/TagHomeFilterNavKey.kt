package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object TagHomeFilterNavKey : ScreenNavKey {
    override val screenName: String get() = "TagHomeFilter"
}
