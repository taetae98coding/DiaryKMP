package io.github.taetae98coding.diary.data.integrity.mapper

import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdictValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

internal fun JsonObject.toDomain(): PlayIntegrityVerdict = PlayIntegrityVerdict(fieldMap = mapValues { (_, element) -> element.toVerdictValue() })

private fun JsonElement.toVerdictValue(): PlayIntegrityVerdictValue =
    when (this) {
        is JsonObject -> PlayIntegrityVerdictValue.Group(fieldMap = mapValues { (_, element) -> element.toVerdictValue() })
        is JsonArray -> PlayIntegrityVerdictValue.ValueList(valueList = map { element -> element.toVerdictValue() })
        is JsonNull -> PlayIntegrityVerdictValue.Null
        is JsonPrimitive -> toVerdictValue()
    }

private fun JsonPrimitive.toVerdictValue(): PlayIntegrityVerdictValue {
    if (isString) return PlayIntegrityVerdictValue.Text(value = content)

    val flag = booleanOrNull
    val number = longOrNull ?: doubleOrNull

    return when {
        flag != null -> PlayIntegrityVerdictValue.Flag(value = flag)
        number != null -> PlayIntegrityVerdictValue.Number(value = number)
        else -> PlayIntegrityVerdictValue.Text(value = content)
    }
}
