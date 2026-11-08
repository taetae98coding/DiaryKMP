package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.work.appaction.core.filterBySearchQuery

public data class AppActionContact(
    val id: String,
    val name: String,
    val nameQualifier: String,
    val description: String,
    val isFavorite: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

internal fun ContactListItem.toAppActionContact(): AppActionContact =
    AppActionContact(
        id = contact.id.toString(),
        name = contact.detail.name,
        nameQualifier = nameQualifier,
        description = contact.detail.description,
        isFavorite = contact.isFavorite,
        createdAt = contact.createdAt.toString(),
        updatedAt = contact.updatedAt.toString(),
    )

public fun List<AppActionContact>.filterBySearchQuery(query: String): List<AppActionContact> = filterBySearchQuery(query = query) { contact -> listOf(contact.name, contact.description) }
