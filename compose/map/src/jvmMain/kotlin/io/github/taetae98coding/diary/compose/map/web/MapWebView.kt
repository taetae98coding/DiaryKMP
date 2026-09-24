package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@Composable
internal fun MapWebView(
    startHttpServer: suspend () -> MapHttpServer?,
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    val httpServer by produceState<MapHttpServer?>(initialValue = null) {
        // 기동 중 화면 이탈로 취소되면 시작된 서버를 닫을 수 없으므로 기동은 취소 없이 끝까지 실행한다.
        val server = withContext(NonCancellable) { startHttpServer() } ?: return@produceState
        value = server
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
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

    val overlayId = remember(webViewPanel) { Uuid.random() }

    MapWebViewOverlayEffect(
        webViewPanel = webViewPanel,
        overlayId = overlayId,
    )

    SwingPanel(
        factory = { webViewPanel },
        modifier = modifier.semantics { mapWebViewOverlayId = overlayId },
    )
}
