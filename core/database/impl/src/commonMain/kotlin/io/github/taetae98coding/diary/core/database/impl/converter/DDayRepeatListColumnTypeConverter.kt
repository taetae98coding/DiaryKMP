package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

// 캘린더 조회가 SQLite JSON 함수(json_each)로 간격을 펼쳐 읽으므로, 간격마다 days나 months 키 하나만 가진 객체 배열로 저장한다.
// 같은 집합이 늘 같은 문자열이 되도록 일 간격, 개월 간격 순으로 놓고 같은 단위 안에서는 작은 수부터 놓는다.
internal class DDayRepeatListColumnTypeConverter {
    @ColumnTypeConverter
    fun repeatSetToText(repeats: Set<DateTimeUnit.DateBased>): String {
        val dayList = repeats.filterIsInstance<DateTimeUnit.DayBased>().map { repeat -> repeat.days }.sorted()
        val monthList = repeats.filterIsInstance<DateTimeUnit.MonthBased>().map { repeat -> repeat.months }.sorted()

        return buildJsonArray {
            dayList.forEach { days -> add(buildJsonObject { put(DAYS_KEY, days) }) }
            monthList.forEach { months -> add(buildJsonObject { put(MONTHS_KEY, months) }) }
        }.toString()
    }

    @ColumnTypeConverter
    fun textToRepeatSet(value: String): Set<DateTimeUnit.DateBased> = Json.parseToJsonElement(value).jsonArray.mapTo(mutableSetOf()) { element -> element.jsonObject.toRepeat() }

    private fun JsonObject.toRepeat(): DateTimeUnit.DateBased {
        val days = get(DAYS_KEY)

        return if (days != null) {
            DateTimeUnit.DayBased(days = days.jsonPrimitive.int)
        } else {
            DateTimeUnit.MonthBased(months = getValue(MONTHS_KEY).jsonPrimitive.int)
        }
    }

    private companion object {
        const val DAYS_KEY: String = "days"
        const val MONTHS_KEY: String = "months"
    }
}
