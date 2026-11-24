package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object DatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
    internal fun providesDiaryDatabase(
        @DiaryDatabaseBuilder
        builder: RoomDatabase.Builder<DiaryDatabase>,
        @DiaryDatabaseDriver
        driver: SQLiteDriver,
    ): DiaryDatabase =
        builder
            .setDriver(driver)
            .build()
}
