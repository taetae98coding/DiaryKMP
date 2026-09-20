package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val TITLE_KEY = "title"
private const val DESCRIPTION_KEY = "description"
private const val PERIOD_KEY = "period"
private const val ALL_DAY_KEY = "allDay"
private const val START_KEY = "start"
private const val END_KEY = "end"
private const val PROMPT_KEY = "prompt"
private const val CURRENT_TITLE_KEY = "currentTitle"
private const val CURRENT_DESCRIPTION_KEY = "currentDescription"
private const val CURRENT_PERIOD_KEY = "currentPeriod"
private const val NOW_KEY = "now"
private const val TIME_ZONE_KEY = "timeZone"
private const val SCHEMA_TYPE_KEY = "type"
private const val SCHEMA_PROPERTIES_KEY = "properties"
private const val SCHEMA_NULLABLE_KEY = "nullable"
private const val SCHEMA_DESCRIPTION_KEY = "description"
private const val OBJECT_TYPE = "OBJECT"
private const val STRING_TYPE = "STRING"
private const val BOOLEAN_TYPE = "BOOLEAN"

private val Midnight = LocalTime(hour = 0, minute = 0)

// 사용자의 언어에 따라 달라지는 문장 대신 키가 고정된 JSON으로 보내, 앱이 지시문을 덧붙이지 않고도 무엇이 무엇인지 드러낸다.
internal fun MemoDraftRequest.toPrompt(): String =
    buildJsonObject {
        if (prompt.isNotBlank()) put(PROMPT_KEY, prompt)
        if (title.isNotBlank()) put(CURRENT_TITLE_KEY, title)
        if (description.isNotBlank()) put(CURRENT_DESCRIPTION_KEY, description)
        dateTime?.let { value -> put(CURRENT_PERIOD_KEY, value.toJsonObject()) }
        put(NOW_KEY, now.toString())
        put(TIME_ZONE_KEY, timeZone.id)
    }.toString()

internal fun JsonObject.toMemoDraft(): MemoDraft =
    MemoDraft(
        title = stringOrEmpty(TITLE_KEY),
        description = stringOrEmpty(DESCRIPTION_KEY),
        dateTime = (this[PERIOD_KEY] as? JsonObject)?.toMemoDateTimeOrNull(),
    )

private fun MemoDateTime.toJsonObject(): JsonObject =
    when (this) {
        is MemoDateTime.AllDay ->
            buildJsonObject {
                put(ALL_DAY_KEY, true)
                put(START_KEY, LocalDateTime(date = dateRange.start, time = Midnight).toString())
                put(END_KEY, LocalDateTime(date = dateRange.endInclusive, time = Midnight).toString())
            }

        is MemoDateTime.DateTime ->
            buildJsonObject {
                put(ALL_DAY_KEY, false)
                put(START_KEY, start.toString())
                put(END_KEY, endInclusive.toString())
            }
    }

private fun JsonObject.toMemoDateTimeOrNull(): MemoDateTime? {
    val start = localDateTimeOrNull(START_KEY)
    val endInclusive = localDateTimeOrNull(END_KEY)

    return when {
        start == null || endInclusive == null -> null
        booleanOrFalse(ALL_DAY_KEY) -> MemoDateTime.AllDay(dateRange = start.date..endInclusive.date)
        else -> MemoDateTime.DateTime(start = start, endInclusive = endInclusive)
    }
}

private fun JsonObject.localDateTimeOrNull(key: String): LocalDateTime? {
    val text = stringOrEmpty(key).takeIf { it.isNotBlank() } ?: return null

    return try {
        LocalDateTime.parse(text)
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun JsonObject.stringOrEmpty(key: String): String =
    (this[key] as? JsonPrimitive)
        ?.takeIf { primitive -> primitive.isString }
        ?.content
        .orEmpty()

private fun JsonObject.booleanOrFalse(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull ?: false

internal val memoDraftResponseSchema: JsonObject =
    buildJsonObject {
        put(SCHEMA_TYPE_KEY, OBJECT_TYPE)
        putJsonObject(SCHEMA_PROPERTIES_KEY) {
            putJsonObject(TITLE_KEY) {
                put(SCHEMA_TYPE_KEY, STRING_TYPE)
                put(SCHEMA_DESCRIPTION_KEY, "One line memo title. Empty when it cannot be decided.")
            }
            putJsonObject(DESCRIPTION_KEY) {
                put(SCHEMA_TYPE_KEY, STRING_TYPE)
                put(SCHEMA_DESCRIPTION_KEY, "Memo description written in Markdown. Empty when it cannot be decided.")
            }
            putJsonObject(PERIOD_KEY) {
                put(SCHEMA_TYPE_KEY, OBJECT_TYPE)
                put(SCHEMA_NULLABLE_KEY, true)
                put(SCHEMA_DESCRIPTION_KEY, "Memo period. Null when it cannot be decided.")
                putJsonObject(SCHEMA_PROPERTIES_KEY) {
                    putJsonObject(ALL_DAY_KEY) {
                        put(SCHEMA_TYPE_KEY, BOOLEAN_TYPE)
                        put(SCHEMA_DESCRIPTION_KEY, "True when the period covers whole days and the time part is ignored.")
                    }
                    putJsonObject(START_KEY) {
                        put(SCHEMA_TYPE_KEY, STRING_TYPE)
                        put(SCHEMA_DESCRIPTION_KEY, "Period start as a local date time in yyyy-MM-ddTHH:mm format.")
                    }
                    putJsonObject(END_KEY) {
                        put(SCHEMA_TYPE_KEY, STRING_TYPE)
                        put(SCHEMA_DESCRIPTION_KEY, "Period end as a local date time in yyyy-MM-ddTHH:mm format. Not earlier than the start.")
                    }
                }
            }
        }
    }
