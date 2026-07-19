package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import kotlinx.serialization.json.Json

internal class WebHeaderListColumnTypeConverter {
    @ColumnTypeConverter
    fun headerListToText(headerList: List<WebHeaderLocalEntity>): String = json.encodeToString(headerList)

    @ColumnTypeConverter
    fun textToHeaderList(value: String): List<WebHeaderLocalEntity> = json.decodeFromString(value)

    private companion object {
        val json: Json = Json
    }
}
