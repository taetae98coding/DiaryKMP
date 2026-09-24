package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class CalendarDatabaseModule {
    @Single
    internal fun providesCalendarDatabase(
        @CalendarDatabaseBuilder
        builder: RoomDatabase.Builder<CalendarDatabase>,
        @CalendarDatabaseDriver
        driver: SQLiteDriver,
    ): CalendarDatabase =
        builder
            .setDriver(driver)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
}
