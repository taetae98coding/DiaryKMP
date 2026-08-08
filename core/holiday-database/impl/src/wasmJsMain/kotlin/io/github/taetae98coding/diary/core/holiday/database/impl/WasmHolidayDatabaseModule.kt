@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseDriver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.w3c.dom.Worker

@Module
@ComponentScan
@Configuration
public class WasmHolidayDatabaseModule {
    @Factory
    @HolidayDatabaseBuilder
    internal fun providesHolidayDatabaseBuilder(): RoomDatabase.Builder<HolidayDatabase> = Room.inMemoryDatabaseBuilder<HolidayDatabase>()

    @Single
    @HolidayDatabaseDriver
    internal fun providesHolidayDatabaseDriver(): SQLiteDriver = WebWorkerSQLiteDriver(createWorker())
}

private fun createWorker(): Worker = js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")
