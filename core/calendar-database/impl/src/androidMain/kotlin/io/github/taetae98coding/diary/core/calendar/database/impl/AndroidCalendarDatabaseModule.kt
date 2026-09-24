package io.github.taetae98coding.diary.core.calendar.database.impl

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidCalendarDatabaseModule {
    @Factory
    @CalendarDatabaseBuilder
    internal fun providesCalendarDatabaseBuilder(context: Context): RoomDatabase.Builder<CalendarDatabase> =
        Room.databaseBuilder(
            context = context,
            name = CalendarDatabase.NAME,
        )
}
