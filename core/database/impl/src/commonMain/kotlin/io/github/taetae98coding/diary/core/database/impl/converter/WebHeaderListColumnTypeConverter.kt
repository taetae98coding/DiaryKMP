package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity

internal class WebHeaderListColumnTypeConverter {
    @ColumnTypeConverter
    fun headerListToText(headerList: List<WebHeaderLocalEntity>): String = codec.encode(headerList)

    @ColumnTypeConverter
    fun textToHeaderList(value: String): List<WebHeaderLocalEntity> = codec.decode(value)

    private companion object {
        val codec: JsonListColumnCodec<WebHeaderLocalEntity> = JsonListColumnCodec(WebHeaderLocalEntity.serializer())
    }
}
