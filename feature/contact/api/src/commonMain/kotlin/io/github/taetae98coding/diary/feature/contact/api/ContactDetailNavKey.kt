package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class ContactDetailNavKey(
    val id: Uuid,
) : ScreenNavKey {
    override val screenName: String get() = "ContactDetail"
}
