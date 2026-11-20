package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun JsonObjectBuilder.put(
    key: String,
    value: Uuid,
) {
    put(key, value.toString())
}

internal fun JsonObjectBuilder.put(
    key: String,
    value: Instant,
) {
    put(key, value.toString())
}

internal fun <T> List<T>.toJsonArray(encode: (T) -> JsonElement): JsonArray = JsonArray(map(encode))

internal fun idResult(id: Uuid): JsonObject = buildJsonObject { put("id", id) }

internal fun changedCountResult(count: Int): JsonObject = buildJsonObject { put("changedCount", count) }

internal fun AppActionId.toJson(): JsonObject = buildJsonObject { put("id", id) }

internal fun AppActionChangedCount.toJson(): JsonObject = changedCountResult(count = changedCount)

internal fun AppActionDone.toJson(): JsonObject = buildJsonObject { put("done", done) }
