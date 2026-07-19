package io.github.taetae98coding.diary.core.database.impl

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidDatabaseModule {
    @Factory
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(context: Context): RoomDatabase.Builder<DiaryDatabase> =
        Room.databaseBuilder(
            context = context,
            name = DiaryDatabase.NAME,
        )
}
