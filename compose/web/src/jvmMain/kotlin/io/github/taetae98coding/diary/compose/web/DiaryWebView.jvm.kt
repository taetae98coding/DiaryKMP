package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel

@Composable
public actual fun DiaryWebView(
    page: WebPage,
    modifier: Modifier,
) {
    SwingPanel(
        factory = ::WebKitWebViewPanel,
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { panel -> panel.loadHtml(html = page.body, baseUrl = page.baseUrl) },
    )
}

@Composable
public actual fun DiaryWebView(
    url: String,
    modifier: Modifier,
) {
    SwingPanel(
        factory = ::WebKitWebViewPanel,
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { panel -> panel.loadUrl(url = url) },
    )
}
