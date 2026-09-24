package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Files

@Module
@ComponentScan
@Configuration
public class JvmCalendarDatabaseModule {
    @Factory
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(
        @CalendarDatabaseDirectory
        databaseDirectory: String,
    ): RoomDatabase.Builder<CalendarDatabase> {
        val databasePath = applicationSupportDirectory(directoryName = databaseDirectory).resolve(CalendarDatabase.NAME)
        databasePath.parent?.let(Files::createDirectories)

        return Room.databaseBuilder<CalendarDatabase>(name = databasePath.toAbsolutePath().toString())
    }
}
