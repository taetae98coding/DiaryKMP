package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.Storage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDispatcher
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class DataStoreModule {
    @Single
    @MapSettingDataStore
    internal fun providesMapSettingDataStore(
        @MapSettingStorage
        storage: Storage<MapSettingData>,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<MapSettingData> = createSettingDataStore(storage = storage, dispatcher = dispatcher, serializer = MapSettingSerializer)

    @Single
    @HolidaySettingDataStore
    internal fun providesHolidaySettingDataStore(
        @HolidaySettingStorage
        storage: Storage<HolidaySettingData>,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<HolidaySettingData> = createSettingDataStore(storage = storage, dispatcher = dispatcher, serializer = HolidaySettingSerializer)

    @Single
    @GeminiSettingDataStore
    internal fun providesGeminiSettingDataStore(
        @GeminiSettingStorage
        storage: Storage<GeminiSettingLocalEntity>,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<GeminiSettingLocalEntity> = createSettingDataStore(storage = storage, dispatcher = dispatcher, serializer = GeminiSettingSerializer)

    internal companion object {
        const val MAP_SETTING_NAME: String = "map-setting.json"
        const val HOLIDAY_SETTING_NAME: String = "holiday-setting.json"
        const val GEMINI_SETTING_NAME: String = "gemini-setting.json"
    }
}

private fun <T> createSettingDataStore(
    storage: Storage<T>,
    dispatcher: CoroutineDispatcher,
    serializer: SettingSerializer<T>,
): DataStore<T> =
    DataStore
        .Builder(
            storage = storage,
            context = dispatcher + SupervisorJob(),
        ).setCorruptionHandler(ReplaceFileCorruptionHandler { serializer.defaultValue })
        .build()
