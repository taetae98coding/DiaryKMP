package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath

@BindingContainer
@ContributesTo(AppScope::class)
public object IosCalendarDatabaseModule {
    @Provides
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(): RoomDatabase.Builder<CalendarDatabase> {
        val path = applicationSupportDirectoryPath() + "/" + CalendarDatabase.NAME

        return Room.databaseBuilder<CalendarDatabase>(name = path)
    }
}
