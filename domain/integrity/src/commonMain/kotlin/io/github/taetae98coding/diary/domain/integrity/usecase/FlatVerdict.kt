package io.github.taetae98coding.diary.domain.integrity.usecase

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

private const val LIST_SEPARATOR = ","
private const val NAME_SEPARATOR = "_"

private class VerdictField(
    val path: List<String>,
    val value: JsonPrimitive,
)

internal fun JsonObject.toFlatVerdict(): JsonObject {
    val fieldList = buildList { collectField(path = emptyList(), element = this@toFlatVerdict) }
    val nameList = fieldList.map { field -> field.path }.toUniqueNameList()

    return JsonObject(nameList.zip(fieldList) { name, field -> name to field.value }.toMap())
}

private fun MutableList<VerdictField>.collectField(
    path: List<String>,
    element: JsonElement,
) {
    when (element) {
        is JsonObject -> element.forEach { (key, value) -> collectField(path = path + key, element = value) }
        is JsonArray -> if (element.isNotEmpty()) add(VerdictField(path = path, value = element.joinToPrimitive()))
        is JsonNull -> Unit
        is JsonPrimitive -> add(VerdictField(path = path, value = element.toVerdictPrimitive()))
    }
}

private fun JsonArray.joinToPrimitive(): JsonPrimitive = JsonPrimitive(joinToString(separator = LIST_SEPARATOR) { element -> (element as? JsonPrimitive)?.content ?: element.toString() })

private fun JsonPrimitive.toVerdictPrimitive(): JsonPrimitive =
    if (!isString && booleanOrNull != null) {
        JsonPrimitive(content)
    } else {
        this
    }

private fun List<List<String>>.toUniqueNameList(): List<String> {
    val depthList = MutableList(size) { 1 }

    while (true) {
        val nameList = mapIndexed { index, path -> path.takeLast(depthList[index]).toFieldName() }
        val duplicatedIndexList =
            nameList.indices.filter { index ->
                nameList.count { name -> name == nameList[index] } > 1 && depthList[index] < this[index].size
            }

        if (duplicatedIndexList.isEmpty()) return nameList

        duplicatedIndexList.forEach { index -> depthList[index] += 1 }
    }
}

private fun List<String>.toFieldName(): String = joinToString(separator = NAME_SEPARATOR) { name -> name.toSnakeCase() }

private fun String.toSnakeCase(): String =
    buildString {
        this@toSnakeCase.forEachIndexed { index, char ->
            if (char.isUpperCase()) {
                if (index > 0) append(NAME_SEPARATOR)
                append(char.lowercaseChar())
            } else {
                append(char)
            }
        }
    }
