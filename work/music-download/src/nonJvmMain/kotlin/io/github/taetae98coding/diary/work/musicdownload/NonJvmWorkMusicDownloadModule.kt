package io.github.taetae98coding.diary.work.musicdownload

import io.github.taetae98coding.diary.domain.playlist.MusicDownloadProxyManager
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadProxyHttpClient
import io.github.taetae98coding.diary.work.musicdownload.proxy.NotProvidedMusicDownloadProxyManager
import io.github.taetae98coding.diary.work.musicdownload.proxy.ProxyDownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.proxy.ProxyMusicDownloader
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

// 프록시는 영상을 다 받아 합친 뒤에야 응답을 시작하므로, 그동안 요청이 조용히 열려 있어도 끊지 않아야 한다.
private val PROXY_CONNECT_TIMEOUT = 10.seconds
private val PROXY_SOCKET_TIMEOUT = 1.hours

@Module
@Configuration
public class NonJvmWorkMusicDownloadModule {
    @Single
    @MusicDownloadProxyHttpClient
    internal fun providesMusicDownloadProxyHttpClient(): HttpClient =
        HttpClient(createPlatformHttpClientEngine()) {
            install(HttpTimeout) {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                connectTimeoutMillis = PROXY_CONNECT_TIMEOUT.inWholeMilliseconds
                socketTimeoutMillis = PROXY_SOCKET_TIMEOUT.inWholeMilliseconds
            }
        }

    @Factory
    internal fun providesDownloadToolPreparer(
        @MusicDownloadProxyHttpClient httpClient: HttpClient,
        musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
    ): DownloadToolPreparer =
        ProxyDownloadToolPreparer(
            httpClient = httpClient,
            musicDownloadProxySettingRepository = musicDownloadProxySettingRepository,
        )

    @Factory
    internal fun providesMusicDownloader(
        @MusicDownloadProxyHttpClient httpClient: HttpClient,
        musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
        @MusicDownloadDispatcher dispatcher: CoroutineDispatcher,
    ): MusicDownloader =
        ProxyMusicDownloader(
            httpClient = httpClient,
            musicDownloadProxySettingRepository = musicDownloadProxySettingRepository,
            dispatcher = dispatcher,
        )

    @Factory
    internal fun providesMusicDownloadProxyManager(): MusicDownloadProxyManager = NotProvidedMusicDownloadProxyManager
}
