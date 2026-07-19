@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.w3c.dom.Worker

@Module
@ComponentScan
@Configuration
public class WasmDatabaseModule {
    @Factory
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(): RoomDatabase.Builder<DiaryDatabase> = Room.inMemoryDatabaseBuilder<DiaryDatabase>()

    @Single
    @DiaryDatabaseDriver
    internal fun providesDiaryDatabaseDriver(): SQLiteDriver = WebWorkerSQLiteDriver(createWorker())
}

private fun createWorker(): Worker = js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")
