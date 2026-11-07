package io.github.taetae98coding.diary.work.mcp.tool.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
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

internal fun JsonObjectBuilder.putIdList(
    key: String,
    value: Collection<Uuid>,
) {
    put(key, JsonArray(value.map { id -> JsonPrimitive(id.toString()) }))
}

internal fun <T> List<T>.toJsonArray(encode: (T) -> JsonElement): JsonArray = JsonArray(map(encode))

internal fun idResult(id: Uuid): JsonObject = buildJsonObject { put("id", id) }

internal fun changedCountResult(count: Int): JsonObject = buildJsonObject { put("changedCount", count) }

internal val doneResult: JsonObject = buildJsonObject { put("done", true) }
