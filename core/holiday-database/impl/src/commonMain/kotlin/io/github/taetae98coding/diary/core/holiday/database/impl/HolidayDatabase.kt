package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.impl.dao.HolidayDao
import io.github.taetae98coding.diary.library.room3.converter.LocalDateColumnTypeConverter

@Database(
    entities = [HolidayLocalEntity::class],
    version = 1,
)
@ConstructedBy(HolidayDatabaseConstructor::class)
@ColumnTypeConverters(LocalDateColumnTypeConverter::class)
internal abstract class HolidayDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao

    companion object {
        const val NAME: String = "holiday.db"
    }
}
