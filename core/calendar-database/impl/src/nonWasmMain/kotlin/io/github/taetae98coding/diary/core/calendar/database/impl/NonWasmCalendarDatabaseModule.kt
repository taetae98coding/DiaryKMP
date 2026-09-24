package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class NonWasmCalendarDatabaseModule {
    @Single
    @CalendarDatabaseDriver
    internal fun providesCalendarDatabaseDriver(): SQLiteDriver = BundledSQLiteDriver()
}
