package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MapWebViewPinMarkersEffect(
    webViewPanel: WebKitWebViewPanel,
    isDocumentReady: StateFlow<Boolean> = MutableStateFlow(false),
    state: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(webViewPanel, isDocumentReady, state) {
        snapshotFlow { state.pins }
            .collect { pins ->
                webViewPanel.evaluateWhenReady(
                    isDocumentReady = isDocumentReady,
                    script = "window.diarySetPins(${pins.toScriptValue()})",
                )
            }
    }
}
