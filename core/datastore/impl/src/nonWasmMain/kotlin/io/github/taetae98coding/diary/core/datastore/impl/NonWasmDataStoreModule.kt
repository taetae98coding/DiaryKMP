package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioStorage
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDispatcher
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.SyncTimeStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class NonWasmDataStoreModule {
    @Factory
    @DiarySettingDispatcher
    internal fun providesDiarySettingDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Single
    @MapSettingStorage
    internal fun providesMapSettingStorage(pathResolver: SettingPathResolver): Storage<MapSettingData> =
        createSettingStorage(
            pathResolver = pathResolver,
            serializer = MapSettingSerializer,
            name = DataStoreModule.MAP_SETTING_NAME,
        )

    @Single
    @HolidaySettingStorage
    internal fun providesHolidaySettingStorage(pathResolver: SettingPathResolver): Storage<HolidaySettingData> =
        createSettingStorage(
            pathResolver = pathResolver,
            serializer = HolidaySettingSerializer,
            name = DataStoreModule.HOLIDAY_SETTING_NAME,
        )

    @Single
    @GeminiSettingStorage
    internal fun providesGeminiSettingStorage(pathResolver: SettingPathResolver): Storage<GeminiSettingLocalEntity> =
        createSettingStorage(
            pathResolver = pathResolver,
            serializer = GeminiSettingSerializer,
            name = DataStoreModule.GEMINI_SETTING_NAME,
        )

    @Single
    @SyncTimeStorage
    internal fun providesSyncTimeStorage(pathResolver: SettingPathResolver): Storage<SyncTimeData> =
        createSettingStorage(
            pathResolver = pathResolver,
            serializer = SyncTimeSerializer,
            name = DataStoreModule.SYNC_TIME_NAME,
        )
}

private fun <T> createSettingStorage(
    pathResolver: SettingPathResolver,
    serializer: SettingSerializer<T>,
    name: String,
): Storage<T> =
    OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = serializer,
        producePath = { pathResolver.resolve(name).toPath() },
    )
