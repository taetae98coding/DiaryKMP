package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object ContactAddNavKey : ScreenNavKey {
    override val screenName: String get() = "ContactAdd"
}
