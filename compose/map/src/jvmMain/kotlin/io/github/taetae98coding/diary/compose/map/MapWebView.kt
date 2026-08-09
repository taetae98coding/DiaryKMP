package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@Composable
internal fun MapWebView(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
    startHttpServer: () -> MapHttpServer?,
) {
    // 서버 기동은 소켓 바인딩까지, 종료는 graceful shutdown까지 호출 스레드를 블로킹하므로
    // composition 스레드(AWT EDT)에서 실행하지 않고 IO 스레드에서 실행한다.
    val httpServer by produceState<MapHttpServer?>(initialValue = null) {
        // 기동 중 화면 이탈로 취소되면 시작된 서버를 닫을 수 없으므로 기동은 취소 없이 끝까지 실행한다.
        val server = withContext(NonCancellable + Dispatchers.IO) { startHttpServer() } ?: return@produceState
        value = server
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                server.close()
            }
        }
    }

    val server = httpServer
    if (server == null) {
        Box(modifier = modifier)
        return
    }

    val webViewPanel =
        remember(server) {
            WebKitWebViewPanel().apply {
                loadUrl(url = server.url)
            }
        }
    val isDocumentReady = remember(webViewPanel) { MutableStateFlow(false) }

    MapWebViewDrainMessageEffect(
        webViewPanel = webViewPanel,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
        isDocumentReady = isDocumentReady,
    )
    MapWebViewSpotMarkerEffect(
        webViewPanel = webViewPanel,
        isDocumentReady = isDocumentReady,
        state = state,
    )
    MapWebViewPinMarkersEffect(
        webViewPanel = webViewPanel,
        isDocumentReady = isDocumentReady,
        state = state,
    )
    MapWebViewMoveCameraEffect(
        webViewPanel = webViewPanel,
        isDocumentReady = isDocumentReady,
        moveCommand = state.moveCommand,
    )

    SwingPanel(
        factory = { webViewPanel },
        modifier = modifier,
    )
}
