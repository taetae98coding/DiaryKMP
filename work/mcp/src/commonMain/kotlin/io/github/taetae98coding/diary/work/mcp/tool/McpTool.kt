package io.github.taetae98coding.diary.work.mcp.tool

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class McpTool(
    val name: String,
    val description: String,
    val parameterList: List<McpToolParameter> = emptyList(),
    val kind: McpToolKind,
    val execute: suspend (McpToolArguments) -> JsonElement,
) {
    val propertySchema: JsonObject
        get() = JsonObject(parameterList.associate { parameter -> parameter.name to parameter.schema() })

    val requiredNameList: List<String>
        get() = parameterList.filter { parameter -> parameter.isRequired }.map { parameter -> parameter.name }
}

internal enum class McpToolKind {
    READ,
    WRITE,
    DESTRUCTIVE,
}

internal class McpToolParameter(
    val name: String,
    val type: McpToolParameterType,
    val description: String,
    val isRequired: Boolean = false,
    val allowedValueList: List<String> = emptyList(),
) {
    fun schema(): JsonObject =
        buildJsonObject {
            when (type) {
                McpToolParameterType.STRING -> put("type", "string")

                McpToolParameterType.INTEGER -> put("type", "integer")

                McpToolParameterType.BOOLEAN -> put("type", "boolean")

                McpToolParameterType.STRING_LIST -> {
                    put("type", "array")
                    put("items", buildJsonObject { put("type", "string") })
                }

                McpToolParameterType.OBJECT -> put("type", "object")
            }
            put("description", description)
            if (allowedValueList.isNotEmpty()) {
                put("enum", JsonArray(allowedValueList.map(::JsonPrimitive)))
            }
        }
}

internal enum class McpToolParameterType {
    STRING,
    INTEGER,
    BOOLEAN,
    STRING_LIST,
    OBJECT,
}
