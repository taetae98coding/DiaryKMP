package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseDriver
import io.github.taetae98coding.diary.library.room3.driver.webWorkerSQLiteDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class WasmHolidayDatabaseModule {
    @Factory
    @HolidayDatabaseBuilder
    internal fun providesHolidayDatabaseBuilder(): RoomDatabase.Builder<HolidayDatabase> = Room.inMemoryDatabaseBuilder<HolidayDatabase>()

    @Single
    @HolidayDatabaseDriver
    internal fun providesHolidayDatabaseDriver(): SQLiteDriver = webWorkerSQLiteDriver()
}
