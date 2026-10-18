package io.github.taetae98coding.diary.domain.integrity.usecase

import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdictValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val LIST_SEPARATOR = ","
private const val NAME_SEPARATOR = "_"

private class VerdictField(
    val path: List<String>,
    val value: JsonPrimitive,
)

internal fun PlayIntegrityVerdict.toFlatVerdict(): JsonObject {
    val fieldList =
        buildList {
            fieldMap.forEach { (key, value) -> collectField(path = listOf(key), value = value) }
        }
    val nameList = fieldList.map { field -> field.path }.toUniqueNameList()

    return JsonObject(nameList.zip(fieldList) { name, field -> name to field.value }.toMap())
}

private fun MutableList<VerdictField>.collectField(
    path: List<String>,
    value: PlayIntegrityVerdictValue,
) {
    when (value) {
        is PlayIntegrityVerdictValue.Group -> value.fieldMap.forEach { (key, child) -> collectField(path = path + key, value = child) }
        is PlayIntegrityVerdictValue.ValueList -> if (value.valueList.isNotEmpty()) add(VerdictField(path = path, value = value.joinToPrimitive()))
        is PlayIntegrityVerdictValue.Null -> Unit
        is PlayIntegrityVerdictValue.Text -> add(VerdictField(path = path, value = JsonPrimitive(value.value)))
        is PlayIntegrityVerdictValue.Number -> add(VerdictField(path = path, value = JsonPrimitive(value.value)))
        is PlayIntegrityVerdictValue.Flag -> add(VerdictField(path = path, value = JsonPrimitive(value.value.toString())))
    }
}

private fun PlayIntegrityVerdictValue.ValueList.joinToPrimitive(): JsonPrimitive =
    JsonPrimitive(
        valueList.joinToString(separator = LIST_SEPARATOR) { value ->
            val element = value.toJsonElement()
            (element as? JsonPrimitive)?.content ?: element.toString()
        },
    )

private fun PlayIntegrityVerdictValue.toJsonElement(): JsonElement =
    when (this) {
        is PlayIntegrityVerdictValue.Group -> JsonObject(fieldMap.mapValues { (_, value) -> value.toJsonElement() })
        is PlayIntegrityVerdictValue.ValueList -> JsonArray(valueList.map { value -> value.toJsonElement() })
        is PlayIntegrityVerdictValue.Null -> JsonNull
        is PlayIntegrityVerdictValue.Text -> JsonPrimitive(value)
        is PlayIntegrityVerdictValue.Number -> JsonPrimitive(value)
        is PlayIntegrityVerdictValue.Flag -> JsonPrimitive(value)
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
