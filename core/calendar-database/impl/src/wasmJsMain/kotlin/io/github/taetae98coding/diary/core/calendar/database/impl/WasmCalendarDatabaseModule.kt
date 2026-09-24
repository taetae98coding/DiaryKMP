package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver
import io.github.taetae98coding.diary.library.room3.driver.webWorkerSQLiteDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class WasmCalendarDatabaseModule {
    @Factory
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(): RoomDatabase.Builder<CalendarDatabase> = Room.inMemoryDatabaseBuilder<CalendarDatabase>()

    @Single
    @CalendarDatabaseDriver
    internal fun providesCalendarDatabaseDriver(): SQLiteDriver = webWorkerSQLiteDriver()
}
