package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val TITLE_KEY = "title"
private const val DESCRIPTION_KEY = "description"
private const val PERIOD_KEY = "period"
private const val ALL_DAY_KEY = "allDay"
private const val START_DATE_KEY = "startDate"
private const val END_DATE_KEY = "endDate"
private const val START_TIME_KEY = "startTime"
private const val END_TIME_KEY = "endTime"
private const val HOUR_KEY = "hour"
private const val MINUTE_KEY = "minute"
private const val PROMPT_KEY = "prompt"
private const val CURRENT_TITLE_KEY = "currentTitle"
private const val CURRENT_DESCRIPTION_KEY = "currentDescription"
private const val CURRENT_PERIOD_KEY = "currentPeriod"
private const val NOW_KEY = "now"
private const val DATE_KEY = "date"
private const val DAY_OF_WEEK_KEY = "dayOfWeek"
private const val TIME_KEY = "time"
private const val TIME_ZONE_KEY = "timeZone"
private const val SCHEMA_TYPE_KEY = "type"
private const val SCHEMA_PROPERTIES_KEY = "properties"
private const val SCHEMA_REQUIRED_KEY = "required"
private const val SCHEMA_FORMAT_KEY = "format"
private const val SCHEMA_MINIMUM_KEY = "minimum"
private const val SCHEMA_MAXIMUM_KEY = "maximum"
private const val SCHEMA_DESCRIPTION_KEY = "description"
private const val OBJECT_TYPE = "object"
private const val STRING_TYPE = "string"
private const val BOOLEAN_TYPE = "boolean"
private const val INTEGER_TYPE = "integer"
private const val NULL_TYPE = "null"
private const val DATE_FORMAT = "date"

private val HourRange = 0..23
private val MinuteRange = 0..59

// 사용자의 언어에 따라 달라지는 문장 대신 키가 고정된 JSON으로 보내, 앱이 지시문을 덧붙이지 않고도 무엇이 무엇인지 드러낸다.
internal fun MemoDraftRequest.toPrompt(): String =
    buildJsonObject {
        if (prompt.isNotBlank()) put(PROMPT_KEY, prompt)
        if (title.isNotBlank()) put(CURRENT_TITLE_KEY, title)
        if (description.isNotBlank()) put(CURRENT_DESCRIPTION_KEY, description)
        dateTime?.let { value -> put(CURRENT_PERIOD_KEY, value.toJsonObject()) }
        putJsonObject(NOW_KEY) {
            put(DATE_KEY, now.date.toString())
            put(DAY_OF_WEEK_KEY, now.dayOfWeek.toRemote())
            put(TIME_KEY, now.time.toJsonObject())
        }
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
                put(START_DATE_KEY, dateRange.start.toString())
                put(END_DATE_KEY, dateRange.endInclusive.toString())
            }

        is MemoDateTime.DateTime ->
            buildJsonObject {
                put(ALL_DAY_KEY, false)
                put(START_DATE_KEY, start.date.toString())
                put(END_DATE_KEY, endInclusive.date.toString())
                put(START_TIME_KEY, start.time.toJsonObject())
                put(END_TIME_KEY, endInclusive.time.toJsonObject())
            }
    }

private fun LocalTime.toJsonObject(): JsonObject =
    buildJsonObject {
        put(HOUR_KEY, hour)
        put(MINUTE_KEY, minute)
    }

private fun DayOfWeek.toRemote(): String =
    when (this) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }

private fun JsonObject.toMemoDateTimeOrNull(): MemoDateTime? {
    val startDate = localDateOrNull(START_DATE_KEY)
    val endDate = localDateOrNull(END_DATE_KEY)
    val startTime = localTimeOrNull(START_TIME_KEY)
    val endTime = localTimeOrNull(END_TIME_KEY)

    return when {
        startDate == null || endDate == null -> null

        booleanOrFalse(ALL_DAY_KEY) -> MemoDateTime.AllDay(dateRange = startDate..endDate)

        startTime == null || endTime == null -> null

        else ->
            MemoDateTime.DateTime(
                start = LocalDateTime(date = startDate, time = startTime),
                endInclusive = LocalDateTime(date = endDate, time = endTime),
            )
    }
}

private fun JsonObject.localDateOrNull(key: String): LocalDate? {
    val text = stringOrEmpty(key).takeIf { it.isNotBlank() } ?: return null

    return try {
        LocalDate.parse(text)
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun JsonObject.localTimeOrNull(key: String): LocalTime? {
    val time = this[key] as? JsonObject
    val hour = time?.intOrNull(HOUR_KEY)?.takeIf { it in HourRange }
    val minute = time?.intOrNull(MINUTE_KEY)?.takeIf { it in MinuteRange }

    return if (hour == null || minute == null) null else LocalTime(hour = hour, minute = minute)
}

private fun JsonObjectBuilder.putType(vararg type: String) {
    if (type.size == 1) {
        put(SCHEMA_TYPE_KEY, type.single())
    } else {
        putJsonArray(SCHEMA_TYPE_KEY) { type.forEach { value -> add(JsonPrimitive(value)) } }
    }
}

private fun JsonObjectBuilder.putRequired(vararg key: String) {
    putJsonArray(SCHEMA_REQUIRED_KEY) { key.forEach { value -> add(JsonPrimitive(value)) } }
}

private fun JsonObjectBuilder.putTimeSchema(
    key: String,
    description: String,
) {
    putJsonObject(key) {
        putType(OBJECT_TYPE, NULL_TYPE)
        put(SCHEMA_DESCRIPTION_KEY, description)
        putJsonObject(SCHEMA_PROPERTIES_KEY) {
            putJsonObject(HOUR_KEY) {
                putType(INTEGER_TYPE)
                put(SCHEMA_MINIMUM_KEY, HourRange.first)
                put(SCHEMA_MAXIMUM_KEY, HourRange.last)
            }
            putJsonObject(MINUTE_KEY) {
                putType(INTEGER_TYPE)
                put(SCHEMA_MINIMUM_KEY, MinuteRange.first)
                put(SCHEMA_MAXIMUM_KEY, MinuteRange.last)
            }
        }
        putRequired(HOUR_KEY, MINUTE_KEY)
    }
}

// 시간대를 붙이는 date-time·time 형식은 모델이 시간대를 잘못 붙이면 시각이 어긋나므로, 날짜는 date 형식으로, 시각은 범위가 강제되는 시와 분의 숫자로 받는다.
internal val memoDraftResponseJsonSchema: JsonObject =
    buildJsonObject {
        putType(OBJECT_TYPE)
        putJsonObject(SCHEMA_PROPERTIES_KEY) {
            putJsonObject(TITLE_KEY) {
                putType(STRING_TYPE)
                put(SCHEMA_DESCRIPTION_KEY, "One line memo title. Empty when it cannot be decided.")
            }
            putJsonObject(DESCRIPTION_KEY) {
                putType(STRING_TYPE)
                put(SCHEMA_DESCRIPTION_KEY, "Memo description written in Markdown. Empty when it cannot be decided.")
            }
            putJsonObject(PERIOD_KEY) {
                putType(OBJECT_TYPE, NULL_TYPE)
                put(SCHEMA_DESCRIPTION_KEY, "Memo period in the time zone of the request, without any offset. Null when it cannot be decided.")
                putJsonObject(SCHEMA_PROPERTIES_KEY) {
                    putJsonObject(ALL_DAY_KEY) {
                        putType(BOOLEAN_TYPE)
                        put(SCHEMA_DESCRIPTION_KEY, "True when the period covers whole days. startTime and endTime are null then.")
                    }
                    putJsonObject(START_DATE_KEY) {
                        putType(STRING_TYPE)
                        put(SCHEMA_FORMAT_KEY, DATE_FORMAT)
                        put(SCHEMA_DESCRIPTION_KEY, "First day of the period.")
                    }
                    putJsonObject(END_DATE_KEY) {
                        putType(STRING_TYPE)
                        put(SCHEMA_FORMAT_KEY, DATE_FORMAT)
                        put(SCHEMA_DESCRIPTION_KEY, "Last day of the period, inclusive. Same as startDate for a period within one day. Not earlier than startDate.")
                    }
                    putTimeSchema(key = START_TIME_KEY, description = "Start time on startDate. Required when allDay is false.")
                    putTimeSchema(key = END_TIME_KEY, description = "End time on endDate. Required when allDay is false. Not earlier than the start.")
                }
                putRequired(ALL_DAY_KEY, START_DATE_KEY, END_DATE_KEY)
            }
        }
        putRequired(TITLE_KEY, DESCRIPTION_KEY, PERIOD_KEY)
    }
