package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import java.nio.file.Files
import java.nio.file.Path

@BindingContainer
@ContributesTo(AppScope::class)
public object JvmCalendarDatabaseModule {
    @Provides
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(
        @CalendarDatabaseDirectory
        databaseDirectory: String,
    ): RoomDatabase.Builder<CalendarDatabase> {
        val databasePath =
            resolveCalendarDatabasePath(
                userHome = userHomeDirectory(),
                databaseDirectory = databaseDirectory,
            )
        databasePath.parent?.let(Files::createDirectories)

        return Room.databaseBuilder<CalendarDatabase>(name = databasePath.toAbsolutePath().toString())
    }
}

internal fun resolveCalendarDatabasePath(
    userHome: Path,
    databaseDirectory: String,
): Path = applicationSupportDirectory(directoryName = databaseDirectory, userHome = userHome).resolve(CalendarDatabase.NAME)
