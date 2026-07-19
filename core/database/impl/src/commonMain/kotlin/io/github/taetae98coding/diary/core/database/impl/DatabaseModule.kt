package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class DatabaseModule {
    @Single
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
