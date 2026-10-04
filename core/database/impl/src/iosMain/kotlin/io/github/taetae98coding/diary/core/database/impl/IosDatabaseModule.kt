package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.library.applicationsupport.documentDirectoryPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class IosDatabaseModule {
    @Factory
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(): RoomDatabase.Builder<DiaryDatabase> {
        val path = documentDirectoryPath() + "/" + DiaryDatabase.NAME

        return Room.databaseBuilder<DiaryDatabase>(name = path)
    }
}
