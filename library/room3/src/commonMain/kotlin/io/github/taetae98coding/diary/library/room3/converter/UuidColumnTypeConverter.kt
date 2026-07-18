package io.github.taetae98coding.diary.library.room3.converter

import androidx.room3.ColumnTypeConverter
import kotlin.uuid.Uuid

public class UuidColumnTypeConverter {
    @ColumnTypeConverter
    public fun uuidToString(uuid: Uuid): String = uuid.toString()

    @ColumnTypeConverter
    public fun stringToUuid(value: String): Uuid = Uuid.parse(value)
}
