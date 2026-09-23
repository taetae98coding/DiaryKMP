package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first

@Composable
internal fun MapWebViewMoveCameraEffect(
    webViewPanel: WebKitWebViewPanel,
    isDocumentReady: StateFlow<Boolean> = MutableStateFlow(false),
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(webViewPanel, isDocumentReady, moveCommand) {
        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            webViewPanel.evaluateWhenReady(
                isDocumentReady = isDocumentReady,
                script = "window.diaryMoveTo(${camera.latitude}, ${camera.longitude})",
            )
        }
    }
}

internal suspend fun WebKitWebViewPanel.evaluateWhenReady(
    isDocumentReady: StateFlow<Boolean>,
    script: String,
) {
    isDocumentReady.first { isReady -> isReady }

    evaluateJavaScript(script)
}
