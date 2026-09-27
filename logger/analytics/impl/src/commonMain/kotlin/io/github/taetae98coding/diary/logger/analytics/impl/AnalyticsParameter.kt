package io.github.taetae98coding.diary.logger.analytics.impl

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

// Google Analytics 4의 이벤트 파라미터 수집 한도. 한도를 넘으면 Firebase SDK가 파라미터나 이벤트 전체를 버리므로 미리 자른다.
private const val MAX_PARAMETER_COUNT = 25
private const val MAX_PARAMETER_NAME_LENGTH = 40
private const val MAX_PARAMETER_VALUE_LENGTH = 100

internal fun JsonObject.toAnalyticsParameterMap(): Map<String, Any> =
    entries
        .asSequence()
        .mapNotNull { (name, value) ->
            val parameterValue =
                when (value) {
                    is JsonNull -> null
                    is JsonPrimitive -> value.toAnalyticsParameterValue()
                    else -> value.toString().take(MAX_PARAMETER_VALUE_LENGTH)
                }

            parameterValue?.let { name.take(MAX_PARAMETER_NAME_LENGTH) to it }
        }.take(MAX_PARAMETER_COUNT)
        .toMap()

private fun JsonPrimitive.toAnalyticsParameterValue(): Any =
    if (isString) {
        content.take(MAX_PARAMETER_VALUE_LENGTH)
    } else {
        longOrNull ?: doubleOrNull ?: content.take(MAX_PARAMETER_VALUE_LENGTH)
    }
