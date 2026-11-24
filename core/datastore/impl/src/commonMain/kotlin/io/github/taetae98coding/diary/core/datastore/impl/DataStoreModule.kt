package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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

@BindingContainer
@ContributesTo(AppScope::class)
public object DataStoreModule {
    internal const val MAP_SETTING_NAME: String = "map-setting.json"
    internal const val MCP_SETTING_NAME: String = "mcp-setting.json"
    internal const val HOLIDAY_SETTING_NAME: String = "holiday-setting.json"
    internal const val GEMINI_SETTING_NAME: String = "gemini-setting.json"
    internal const val SYNC_TIME_NAME: String = "sync-time.json"
    internal const val FILE_DOWNLOAD_SETTING_NAME: String = "file-download-setting.json"
    internal const val WEB_LOGIN_NAME: String = "web-login.json"
    internal const val LOGIN_PROVIDER_NAME: String = "login-provider.json"

    @Provides
    @SingleIn(AppScope::class)
    @MapSettingDataStore
    internal fun providesMapSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<MapSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = MapSettingSerializer, name = MAP_SETTING_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @McpSettingDataStore
    internal fun providesMcpSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<McpSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = McpSettingSerializer, name = MCP_SETTING_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @HolidaySettingDataStore
    internal fun providesHolidaySettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<HolidaySettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = HolidaySettingSerializer, name = HOLIDAY_SETTING_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @GeminiSettingDataStore
    internal fun providesGeminiSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<GeminiSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = GeminiSettingSerializer, name = GEMINI_SETTING_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @FileDownloadSettingDataStore
    internal fun providesFileDownloadSettingDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<FileDownloadSettingData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = FileDownloadSettingSerializer, name = FILE_DOWNLOAD_SETTING_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @SyncTimeDataStore
    internal fun providesSyncTimeDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<SyncTimeData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = SyncTimeSerializer, name = SYNC_TIME_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @WebLoginDataStore
    internal fun providesWebLoginDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<WebLoginData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = WebLoginSerializer, name = WEB_LOGIN_NAME)

    @Provides
    @SingleIn(AppScope::class)
    @LoginProviderDataStore
    internal fun providesLoginProviderDataStore(
        storageFactory: SettingStorageFactory,
        @DiarySettingDispatcher
        dispatcher: CoroutineDispatcher,
    ): DataStore<LoginProviderData> = createSettingDataStore(storageFactory = storageFactory, dispatcher = dispatcher, serializer = LoginProviderSerializer, name = LOGIN_PROVIDER_NAME)
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
