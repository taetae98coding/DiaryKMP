package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class IosCalendarDatabaseModule {
    @Factory
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(): RoomDatabase.Builder<CalendarDatabase> {
        val path = applicationSupportDirectoryPath() + "/" + CalendarDatabase.NAME

        return Room.databaseBuilder<CalendarDatabase>(name = path)
    }
}
