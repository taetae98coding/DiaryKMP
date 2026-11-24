package io.github.taetae98coding.diary.core.calendar.database.impl

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidCalendarDatabaseModule {
    @Provides
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(context: Context): RoomDatabase.Builder<CalendarDatabase> =
        Room.databaseBuilder(
            context = context,
            name = CalendarDatabase.NAME,
        )
}
