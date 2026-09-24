package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class HolidayDatabaseModule {
    @Single
    internal fun providesHolidayDatabase(
        @HolidayDatabaseBuilder
        builder: RoomDatabase.Builder<HolidayDatabase>,
        @HolidayDatabaseDriver
        driver: SQLiteDriver,
    ): HolidayDatabase =
        builder
            .setDriver(driver)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
}
