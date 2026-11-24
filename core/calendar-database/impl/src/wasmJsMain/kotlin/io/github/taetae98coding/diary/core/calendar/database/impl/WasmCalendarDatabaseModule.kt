package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver
import io.github.taetae98coding.diary.library.room3.driver.webWorkerSQLiteDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object WasmCalendarDatabaseModule {
    @Provides
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(): RoomDatabase.Builder<CalendarDatabase> = Room.inMemoryDatabaseBuilder<CalendarDatabase>()

    @Provides
    @SingleIn(AppScope::class)
    @CalendarDatabaseDriver
    internal fun providesCalendarDatabaseDriver(): SQLiteDriver = webWorkerSQLiteDriver()
}
