package io.github.taetae98coding.diary.core.database.impl

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver

@BindingContainer
@ContributesTo(AppScope::class)
public object NonWasmDatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
    @DiaryDatabaseDriver
    internal fun providesDiaryDatabaseDriver(): SQLiteDriver = BundledSQLiteDriver()
}
