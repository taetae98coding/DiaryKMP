package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// 도구가 내보내는 JSON의 기대값을 공통 동작의 결과를 거치지 않고 도메인 모델에서 직접 만든다.
internal fun Memo.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", detail.title)
        put("description", detail.description)
        put("color", detail.color.toColorHex())
        put("dateTime", detail.dateTime?.toJson() ?: JsonNull)
        put("primaryTagId", primaryTagId?.toString())
        put("isFinished", isFinished)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

internal fun MemoDateTime.toJson(): JsonObject =
    when (this) {
        is MemoDateTime.AllDay ->
            buildJsonObject {
                put("allDay", true)
                put("start", dateRange.start.toString())
                put("endInclusive", dateRange.endInclusive.toString())
            }

        is MemoDateTime.DateTime ->
            buildJsonObject {
                put("allDay", false)
                put("start", start.toString())
                put("endInclusive", endInclusive.toString())
            }
    }

internal fun ContactListItem.toJson(): JsonObject =
    buildJsonObject {
        put("id", contact.id)
        put("name", contact.detail.name)
        put("nameQualifier", nameQualifier)
        put("description", contact.detail.description)
        put("isFavorite", contact.isFavorite)
        put("createdAt", contact.createdAt)
        put("updatedAt", contact.updatedAt)
    }
