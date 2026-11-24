package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import io.github.taetae98coding.diary.library.webkit.hideWhileCoveredByOverlay
import io.github.taetae98coding.diary.library.webkit.webKitWebViewOverlayId
import kotlin.uuid.Uuid

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<WebViewContent>())
internal class JvmWebViewContent : WebViewContent {
    @Composable
    override fun UrlContent(
        url: String,
        modifier: Modifier,
    ) {
        WebKitWebView(
            modifier = modifier,
            update = { panel -> panel.loadUrl(url = url) },
        )
    }

    @Composable
    override fun PageContent(
        page: WebPage,
        modifier: Modifier,
    ) {
        WebKitWebView(
            modifier = modifier,
            update = { panel -> panel.loadHtml(html = page.body, baseUrl = page.baseUrl) },
        )
    }
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
                .testTag(WEB_VIEW_TEST_TAG)
                .semantics { webKitWebViewOverlayId = overlayId },
        update = update,
    )
}
