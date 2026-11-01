package io.github.taetae98coding.diary.data.memo.mapper

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

internal fun JsonObject.intOrNull(key: String): Int? =
    (this[key] as? JsonPrimitive)
        ?.takeUnless { primitive -> primitive.isString }
        ?.intOrNull

internal fun JsonObject.stringOrEmpty(key: String): String =
    (this[key] as? JsonPrimitive)
        ?.takeIf { primitive -> primitive.isString }
        ?.content
        .orEmpty()

internal fun JsonObject.booleanOrFalse(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull ?: false
