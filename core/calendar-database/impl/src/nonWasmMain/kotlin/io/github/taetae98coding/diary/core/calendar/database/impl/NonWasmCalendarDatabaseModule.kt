package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object NonWasmCalendarDatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
    @CalendarDatabaseDriver
    internal fun providesCalendarDatabaseDriver(): SQLiteDriver = BundledSQLiteDriver()
}
