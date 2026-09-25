package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import io.github.taetae98coding.diary.library.webkit.hideWhileCoveredByOverlay
import io.github.taetae98coding.diary.library.webkit.webKitWebViewOverlayId
import kotlin.uuid.Uuid

@Composable
public actual fun DiaryWebView(
    page: WebPage,
    modifier: Modifier,
) {
    WebKitWebView(
        modifier = modifier,
        update = { panel -> panel.loadHtml(html = page.body, baseUrl = page.baseUrl) },
    )
}

@Composable
internal actual fun DiaryUrlWebView(
    url: String,
    modifier: Modifier,
) {
    WebKitWebView(
        modifier = modifier,
        update = { panel -> panel.loadUrl(url = url) },
    )
}

@Composable
private fun WebKitWebView(
    modifier: Modifier,
    update: (WebKitWebViewPanel) -> Unit,
) {
    val webViewPanel = remember { WebKitWebViewPanel() }
    val overlayId = remember(webViewPanel) { Uuid.random() }

    LaunchedEffect(webViewPanel, overlayId) {
        webViewPanel.hideWhileCoveredByOverlay(overlayId = overlayId)
    }

    SwingPanel(
        factory = { webViewPanel },
        modifier =
            modifier
                .testTag(DIARY_WEB_VIEW_TEST_TAG)
                .semantics { webKitWebViewOverlayId = overlayId },
        update = update,
    )
}
