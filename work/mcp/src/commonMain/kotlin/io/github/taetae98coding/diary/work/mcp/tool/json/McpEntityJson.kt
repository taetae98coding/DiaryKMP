package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val COLOR = "color"
private const val CREATED_AT = "createdAt"
private const val DESCRIPTION = "description"
private const val TITLE = "title"
private const val UPDATED_AT = "updatedAt"

internal fun Memo.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put(COLOR, detail.color.toColorHex())
        put("dateTime", detail.dateTime?.toJson() ?: JsonNull)
        put("primaryTagId", primaryTagId?.toString())
        put("isFinished", isFinished)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun Tag.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("emoji", detail.emoji)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put(COLOR, detail.color.toColorHex())
        put("isFinished", isFinished)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun Place.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put(COLOR, detail.color.toColorHex())
        put("address", detail.address)
        put("latitude", detail.coordinate.latitude.takeIf(Double::isFinite))
        put("longitude", detail.coordinate.longitude.takeIf(Double::isFinite))
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun Web.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put("url", detail.url)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun ContactListItem.toJson(): JsonObject =
    buildJsonObject {
        put("id", contact.id)
        put("name", contact.detail.name)
        put("nameQualifier", nameQualifier)
        put(DESCRIPTION, contact.detail.description)
        put("isFavorite", contact.isFavorite)
        put(CREATED_AT, contact.createdAt)
        put(UPDATED_AT, contact.updatedAt)
    }
