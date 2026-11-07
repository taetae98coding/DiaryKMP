package io.github.taetae98coding.diary.work.mcp.tool

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

internal class McpToolArguments(
    private val jsonObject: JsonObject,
) {
    fun has(name: String): Boolean = jsonObject[name].let { element -> element != null && element !is JsonNull }

    fun stringOrNull(name: String): String? =
        when (val element = jsonObject[name]) {
            null, JsonNull -> null
            is JsonPrimitive -> if (element.isString) element.content else invalid(name, "a string")
            else -> invalid(name, "a string")
        }

    fun string(name: String): String = stringOrNull(name) ?: missing(name)

    fun intOrNull(name: String): Int? =
        when (val element = jsonObject[name]) {
            null, JsonNull -> null
            is JsonPrimitive -> element.intOrNull ?: invalid(name, "an integer")
            else -> invalid(name, "an integer")
        }

    fun booleanOrNull(name: String): Boolean? =
        when (val element = jsonObject[name]) {
            null, JsonNull -> null
            is JsonPrimitive -> element.booleanOrNull ?: invalid(name, "a boolean")
            else -> invalid(name, "a boolean")
        }

    fun boolean(name: String): Boolean = booleanOrNull(name) ?: missing(name)

    fun stringList(name: String): List<String> =
        when (val element = jsonObject[name]) {
            null, JsonNull -> emptyList()
            is JsonArray -> element.map { item -> (item as? JsonPrimitive)?.takeIf { it.isString }?.content ?: invalid(name, "a list of strings") }
            else -> invalid(name, "a list of strings")
        }

    fun objectOrNull(name: String): JsonObject? =
        when (val element = jsonObject[name]) {
            null, JsonNull -> null
            is JsonObject -> element
            else -> invalid(name, "an object")
        }

    fun element(name: String): JsonElement? = jsonObject[name]?.takeUnless { element -> element is JsonNull }

    internal fun missing(name: String): Nothing = throw McpToolArgumentException(message = "'$name' is required.")

    internal fun invalid(
        name: String,
        expected: String,
    ): Nothing = throw McpToolArgumentException(message = "'$name' must be $expected.")
}
