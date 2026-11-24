package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath

@BindingContainer
@ContributesTo(AppScope::class)
public object IosDatabaseModule {
    @Provides
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(): RoomDatabase.Builder<DiaryDatabase> {
        val path = applicationSupportDirectoryPath() + "/" + DiaryDatabase.NAME

        return Room.databaseBuilder<DiaryDatabase>(name = path)
    }
}
