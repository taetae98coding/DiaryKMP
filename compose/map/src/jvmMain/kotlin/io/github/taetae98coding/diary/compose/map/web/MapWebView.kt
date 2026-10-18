package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import io.github.taetae98coding.diary.library.webkit.hideWhileCoveredByOverlay
import io.github.taetae98coding.diary.library.webkit.webKitWebViewOverlayId
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
internal fun MapWebView(
    createHtml: () -> String?,
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    val httpServer = koinInject<MapHttpServer>()
    // 지도 문서에는 처음 그릴 때의 위치와 핀을 담고, 이후 바뀐 상태는 스크립트로 전달한다.
    val page = remember(httpServer) { createHtml()?.let(httpServer::register) }

    DisposableEffect(page) {
        onDispose { page?.close() }
    }

    if (page == null) {
        Box(modifier = modifier)
        return
    }

    val webViewPanel =
        remember(page) {
            WebKitWebViewPanel().apply {
                loadUrl(url = page.url)
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

    LaunchedEffect(webViewPanel, overlayId) {
        webViewPanel.hideWhileCoveredByOverlay(overlayId = overlayId)
    }

    SwingPanel(
        factory = { webViewPanel },
        modifier = modifier.semantics { webKitWebViewOverlayId = overlayId },
    )
}
