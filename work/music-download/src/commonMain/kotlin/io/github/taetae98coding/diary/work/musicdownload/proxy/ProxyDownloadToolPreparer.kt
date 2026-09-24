package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPrepareResult
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

internal const val PROXY_HEALTH_TIMEOUT_MILLIS: Long = 10_000L

internal class ProxyDownloadToolPreparer(
    private val httpClient: HttpClient,
    private val musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
) : DownloadToolPreparer {
    override suspend fun prepare(): DownloadToolPrepareResult {
        val address = musicDownloadProxySettingRepository.get().first().address
        if (address.isBlank()) return DownloadToolPrepareResult.ProxyNotConfigured

        return try {
            val response =
                httpClient.get(urlString = address.toMusicDownloadProxyHealthUrl()) {
                    // 연결 확인은 곡을 기다리는 요청과 달리 프록시가 없으면 빨리 끝나야 한다.
                    timeout { requestTimeoutMillis = PROXY_HEALTH_TIMEOUT_MILLIS }
                }

            if (response.status.isSuccess()) DownloadToolPrepareResult.Prepared else DownloadToolPrepareResult.ProxyUnreachable
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            DownloadToolPrepareResult.ProxyUnreachable
        }
    }
}
