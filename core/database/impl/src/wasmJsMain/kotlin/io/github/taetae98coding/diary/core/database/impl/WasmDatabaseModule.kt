package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver
import io.github.taetae98coding.diary.library.room3.driver.webWorkerSQLiteDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object WasmDatabaseModule {
    @Provides
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(): RoomDatabase.Builder<DiaryDatabase> = Room.inMemoryDatabaseBuilder<DiaryDatabase>()

    @Provides
    @SingleIn(AppScope::class)
    @DiaryDatabaseDriver
    internal fun providesDiaryDatabaseDriver(): SQLiteDriver = webWorkerSQLiteDriver()
}
