package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object ContactAddNavKey : ScreenNavKey {
    override val screenName: String get() = "ContactAdd"
}
