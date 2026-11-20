package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDetail
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val COLOR = "color"
private const val CREATED_AT = "createdAt"
private const val DESCRIPTION = "description"
private const val ID = "id"
private const val IS_FINISHED = "isFinished"
private const val TITLE = "title"
private const val UPDATED_AT = "updatedAt"

internal fun Tag.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put("emoji", detail.emoji)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put(COLOR, detail.color.toColorHex())
        put(IS_FINISHED, isFinished)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun AppActionMemo.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put(TITLE, title)
        put(DESCRIPTION, description)
        put(COLOR, color)
        put("dateTime", dateTime?.toJson() ?: JsonNull)
        put("primaryTagId", primaryTagId)
        put(IS_FINISHED, isFinished)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun AppActionMemoDetail.toJson(): JsonObject =
    buildJsonObject {
        put("memo", memo.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
        put("places", places.toJsonArray { place -> place.toJson() })
        put("webs", webs.toJsonArray { web -> web.toJson() })
        put("contacts", contacts.toJsonArray { contact -> contact.toJson() })
    }

internal fun AppActionTag.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put("emoji", emoji)
        put(TITLE, title)
        put(DESCRIPTION, description)
        put(COLOR, color)
        put(IS_FINISHED, isFinished)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun AppActionPlace.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put(TITLE, title)
        put(DESCRIPTION, description)
        put(COLOR, color)
        put("address", address)
        put("latitude", latitude)
        put("longitude", longitude)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun AppActionWeb.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put(TITLE, title)
        put(DESCRIPTION, description)
        put("url", url)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun AppActionContact.toJson(): JsonObject =
    buildJsonObject {
        put(ID, id)
        put("name", name)
        put("nameQualifier", nameQualifier)
        put(DESCRIPTION, description)
        put("isFavorite", isFavorite)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }
