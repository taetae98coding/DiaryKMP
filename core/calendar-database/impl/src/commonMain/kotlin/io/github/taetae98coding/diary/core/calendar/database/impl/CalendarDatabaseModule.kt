package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object CalendarDatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
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
