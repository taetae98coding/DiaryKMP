package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.WebLocalStorage
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.BrowserSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDispatcher
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.HolidaySettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.MusicDownloadProxySettingStorage
import io.github.taetae98coding.diary.core.datastore.impl.di.SyncTimeStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmDataStoreModule {
    @Factory
    @DiarySettingDispatcher
    internal fun providesDiarySettingDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Single
    @MapSettingStorage
    internal fun providesMapSettingStorage(): Storage<MapSettingData> =
        WebLocalStorage(
            serializer = MapSettingSerializer,
            name = DataStoreModule.MAP_SETTING_NAME,
        )

    @Single
    @HolidaySettingStorage
    internal fun providesHolidaySettingStorage(): Storage<HolidaySettingData> =
        WebLocalStorage(
            serializer = HolidaySettingSerializer,
            name = DataStoreModule.HOLIDAY_SETTING_NAME,
        )

    @Single
    @GeminiSettingStorage
    internal fun providesGeminiSettingStorage(): Storage<GeminiSettingLocalEntity> =
        WebLocalStorage(
            serializer = GeminiSettingSerializer,
            name = DataStoreModule.GEMINI_SETTING_NAME,
        )

    @Single
    @BrowserSettingStorage
    internal fun providesBrowserSettingStorage(): Storage<BrowserSettingData> =
        WebLocalStorage(
            serializer = BrowserSettingSerializer,
            name = DataStoreModule.BROWSER_SETTING_NAME,
        )

    @Single
    @MusicDownloadProxySettingStorage
    internal fun providesMusicDownloadProxySettingStorage(): Storage<MusicDownloadProxySettingData> =
        WebLocalStorage(
            serializer = MusicDownloadProxySettingSerializer,
            name = DataStoreModule.MUSIC_DOWNLOAD_PROXY_SETTING_NAME,
        )

    @Single
    @SyncTimeStorage
    internal fun providesSyncTimeStorage(): Storage<SyncTimeData> =
        WebLocalStorage(
            serializer = SyncTimeSerializer,
            name = DataStoreModule.SYNC_TIME_NAME,
        )
}
