package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadScope
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private const val ALL_INTERFACES_HOST = "0.0.0.0"

// 0을 넘기면 운영체제가 비어 있는 포트를 골라 주므로 다른 프로그램과 겹치지 않는다.
private const val ANY_PORT = 0

@Single(createdAtStart = true)
internal class MusicDownloadProxyServer(
    private val handler: MusicDownloadProxyHandler,
    private val networkAddressSource: NetworkAddressSource,
    @param:MusicDownloadScope scope: CoroutineScope,
    @param:MusicDownloadDispatcher dispatcher: CoroutineDispatcher,
) {
    val status: StateFlow<MusicDownloadProxyStatus?>
        field = MutableStateFlow<MusicDownloadProxyStatus?>(null)

    init {
        // 서버는 자기 코루틴을 시작한 코루틴에 붙여 두므로, 상태를 정한 뒤에도 이 코루틴은 서버와 함께 살아 있다.
        scope.launch(dispatcher) { start() }
    }

    suspend fun start() {
        status.value =
            try {
                val server = embeddedServer(factory = CIO, port = ANY_PORT, host = ALL_INTERFACES_HOST) { musicDownloadProxy(handler = handler) }
                // start()는 안에서 runBlocking으로 기다려 코루틴 스레드를 막으므로 suspend 버전으로 시작한다.
                server.startSuspend(wait = false)
                val port =
                    server.engine
                        .resolvedConnectors()
                        .first()
                        .port

                MusicDownloadProxyStatus.Serving(addressList = networkAddressSource.findAddressList().toMusicDownloadProxyAddressList(port = port))
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                DiaryLogger.log(log = ConsoleLog(tag = TAG, message = "다운로드 프록시 시작 실패", throwable = throwable))
                MusicDownloadProxyStatus.Unavailable
            }
    }

    private companion object {
        const val TAG: String = "MusicDownloadProxyServer"
    }
}
