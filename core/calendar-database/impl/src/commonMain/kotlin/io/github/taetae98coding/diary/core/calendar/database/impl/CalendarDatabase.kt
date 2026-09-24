package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.impl.converter.HolidayCountryColumnTypeConverter
import io.github.taetae98coding.diary.core.calendar.database.impl.dao.HolidayDao
import io.github.taetae98coding.diary.library.room3.converter.LocalDateColumnTypeConverter

@Database(
    entities = [HolidayLocalEntity::class],
    version = 1,
)
@ConstructedBy(CalendarDatabaseConstructor::class)
@ColumnTypeConverters(
    LocalDateColumnTypeConverter::class,
    HolidayCountryColumnTypeConverter::class,
)
internal abstract class CalendarDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao

    companion object {
        const val NAME: String = "calendar.db"
    }
}
