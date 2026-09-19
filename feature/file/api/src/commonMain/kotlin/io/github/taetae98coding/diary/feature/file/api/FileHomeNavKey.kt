package io.github.taetae98coding.diary.feature.file.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object FileHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "FileHome"
}
