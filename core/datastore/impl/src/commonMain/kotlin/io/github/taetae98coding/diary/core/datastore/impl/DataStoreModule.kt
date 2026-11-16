package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDispatcher
import io.github.taetae98coding.diary.core.datastore.impl.di.FileDownloadSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.LoginProviderDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.McpSettingDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.SyncTimeDataStore
import io.github.taetae98coding.diary.core.datastore.impl.di.WebLoginDataStore
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
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<MapSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = MapSettingSerializer, name = MAP_SETTING_NAME)

    @Single
    @McpSettingDataStore
    internal fun providesMcpSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<McpSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = McpSettingSerializer, name = MCP_SETTING_NAME)

    @Single
    @HolidaySettingDataStore
    internal fun providesHolidaySettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<HolidaySettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = HolidaySettingSerializer, name = HOLIDAY_SETTING_NAME)

    @Single
    @GeminiSettingDataStore
    internal fun providesGeminiSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<GeminiSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = GeminiSettingSerializer, name = GEMINI_SETTING_NAME)

    @Single
    @FileDownloadSettingDataStore
    internal fun providesFileDownloadSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<FileDownloadSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = FileDownloadSettingSerializer, name = FILE_DOWNLOAD_SETTING_NAME)

    @Single
    @SyncTimeDataStore
    internal fun providesSyncTimeDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<SyncTimeData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = SyncTimeSerializer, name = SYNC_TIME_NAME)

    @Single
    @WebLoginDataStore
    internal fun providesWebLoginDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<WebLoginData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = WebLoginSerializer, name = WEB_LOGIN_NAME)

    @Single
    @LoginProviderDataStore
    internal fun providesLoginProviderDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<LoginProviderData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = LoginProviderSerializer, name = LOGIN_PROVIDER_NAME)

    internal companion object {
        const val MAP_SETTING_NAME: String = "map-setting.json"
        const val MCP_SETTING_NAME: String = "mcp-setting.json"
        const val HOLIDAY_SETTING_NAME: String = "holiday-setting.json"
        const val GEMINI_SETTING_NAME: String = "gemini-setting.json"
        const val SYNC_TIME_NAME: String = "sync-time.json"
        const val FILE_DOWNLOAD_SETTING_NAME: String = "file-download-setting.json"
        const val WEB_LOGIN_NAME: String = "web-login.json"
        const val LOGIN_PROVIDER_NAME: String = "login-provider.json"
    }
}

private fun <T> createSettingDataStore(
    storageFactory: SettingStorageFactory,
    dispatcher: CoroutineDispatcher,
    serializer: SettingSerializer<T>,
    name: String,
): DataStore<T> =
    DataStore
        .Builder(
            storage = storageFactory.create(serializer = serializer, name = name),
            context = dispatcher + SupervisorJob(),
        ).setCorruptionHandler(ReplaceFileCorruptionHandler { serializer.defaultValue })
        .build()
