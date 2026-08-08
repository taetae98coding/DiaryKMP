package io.github.taetae98coding.diary.core.holiday.database.impl

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidHolidayDatabaseModule {
    @Factory
    @HolidayDatabaseBuilder
    internal fun providesHolidayDatabaseBuilder(context: Context): RoomDatabase.Builder<HolidayDatabase> =
        Room.databaseBuilder(
            context = context,
            name = HolidayDatabase.NAME,
        )
}
