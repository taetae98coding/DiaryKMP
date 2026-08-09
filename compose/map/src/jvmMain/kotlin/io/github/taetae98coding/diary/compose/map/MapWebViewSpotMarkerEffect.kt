package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MapWebViewSpotMarkerEffect(
    webViewPanel: WebKitWebViewPanel,
    isDocumentReady: StateFlow<Boolean> = MutableStateFlow(false),
    state: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(webViewPanel, isDocumentReady, state) {
        snapshotFlow { state.spot }
            .collect { spot ->
                val script =
                    if (spot == null || !spot.isFinite) {
                        "window.diaryClearSpot()"
                    } else {
                        "window.diaryShowSpot(${spot.latitude}, ${spot.longitude})"
                    }

                webViewPanel.evaluateWhenReady(isDocumentReady = isDocumentReady, script = script)
            }
    }
}
