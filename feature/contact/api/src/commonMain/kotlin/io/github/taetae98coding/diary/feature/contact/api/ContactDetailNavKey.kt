package io.github.taetae98coding.diary.feature.contact.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class ContactDetailNavKey(
    val id: Uuid,
) : NavKey
