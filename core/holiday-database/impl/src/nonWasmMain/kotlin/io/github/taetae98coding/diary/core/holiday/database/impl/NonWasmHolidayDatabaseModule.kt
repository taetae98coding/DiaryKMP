package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseDriver
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class NonWasmHolidayDatabaseModule {
    @Single
    @HolidayDatabaseDriver
    internal fun providesHolidayDatabaseDriver(): SQLiteDriver = BundledSQLiteDriver()
}
