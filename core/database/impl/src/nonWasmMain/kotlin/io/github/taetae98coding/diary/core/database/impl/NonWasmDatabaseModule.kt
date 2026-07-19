package io.github.taetae98coding.diary.core.database.impl

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class NonWasmDatabaseModule {
    @Single
    @DiaryDatabaseDriver
    internal fun providesDiaryDatabaseDriver(): SQLiteDriver = BundledSQLiteDriver()
}
