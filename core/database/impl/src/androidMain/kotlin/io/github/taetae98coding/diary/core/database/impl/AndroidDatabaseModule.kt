package io.github.taetae98coding.diary.core.database.impl

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidDatabaseModule {
    @Provides
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(context: Context): RoomDatabase.Builder<DiaryDatabase> =
        Room.databaseBuilder(
            context = context,
            name = DiaryDatabase.NAME,
        )
}
