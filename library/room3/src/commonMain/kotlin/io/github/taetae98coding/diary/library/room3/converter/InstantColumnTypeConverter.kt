package io.github.taetae98coding.diary.library.room3.converter

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

public class InstantColumnTypeConverter {
    @ColumnTypeConverter
    public fun instantToEpochMilliseconds(instant: Instant): Long = instant.toEpochMilliseconds()

    @ColumnTypeConverter
    public fun epochMillisecondsToInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)
}
