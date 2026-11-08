package io.github.taetae98coding.diary.work.appaction.appfunction.item

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

/** A tag. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionTag(
    /** Id of the tag. */
    val id: String,
    /** Emoji of the tag. */
    val emoji: String,
    /** Title of the tag. */
    val title: String,
    /** Description of the tag. */
    val description: String,
    /** Color of the tag as a hex color like #4285F4. */
    val color: String,
    /** Whether the tag is finished. */
    val isFinished: Boolean,
    /** When the tag was created, in ISO-8601. */
    val createdAt: String,
    /** When the tag was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A saved place. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionPlace(
    /** Id of the place. */
    val id: String,
    /** Title of the place. */
    val title: String,
    /** Description of the place. */
    val description: String,
    /** Color of the place as a hex color like #4285F4. */
    val color: String,
    /** Address of the place. */
    val address: String,
    /** Latitude of the place. Null when the place has no coordinate. */
    val latitude: Double?,
    /** Longitude of the place. Null when the place has no coordinate. */
    val longitude: Double?,
    /** When the place was created, in ISO-8601. */
    val createdAt: String,
    /** When the place was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A saved web page. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionWeb(
    /** Id of the web. */
    val id: String,
    /** Title of the web. */
    val title: String,
    /** Description of the web. */
    val description: String,
    /** Address of the web page. */
    val url: String,
    /** When the web was created, in ISO-8601. */
    val createdAt: String,
    /** When the web was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A contact. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionContact(
    /** Id of the contact. */
    val id: String,
    /** Name of the contact. */
    val name: String,
    /** Text that tells contacts with the same name apart. Empty when not needed. */
    val nameQualifier: String,
    /** Description of the contact. */
    val description: String,
    /** Whether the contact is a favorite. */
    val isFavorite: Boolean,
    /** When the contact was created, in ISO-8601. */
    val createdAt: String,
    /** When the contact was last updated, in ISO-8601. */
    val updatedAt: String,
)

internal fun AppActionTag.toAppFunctionTag(): AppFunctionTag =
    AppFunctionTag(
        id = id,
        emoji = emoji,
        title = title,
        description = description,
        color = color,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionPlace.toAppFunctionPlace(): AppFunctionPlace =
    AppFunctionPlace(
        id = id,
        title = title,
        description = description,
        color = color,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionWeb.toAppFunctionWeb(): AppFunctionWeb =
    AppFunctionWeb(
        id = id,
        title = title,
        description = description,
        url = url,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionContact.toAppFunctionContact(): AppFunctionContact =
    AppFunctionContact(
        id = id,
        name = name,
        nameQualifier = nameQualifier,
        description = description,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
