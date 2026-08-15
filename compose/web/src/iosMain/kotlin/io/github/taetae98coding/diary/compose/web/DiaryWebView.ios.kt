package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.UIKitView
import io.github.taetae98coding.diary.core.model.web.WebPage
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Composable
public actual fun DiaryWebView(
    page: WebPage,
    modifier: Modifier,
) {
    UIKitView(
        factory = { WKWebView() },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView ->
            webView.loadHTMLString(page.body, NSURL.URLWithString(page.baseUrl))
        },
    )
}

@Composable
public actual fun DiaryWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        factory = { WKWebView() },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView ->
            NSURL.URLWithString(url)?.let { nsUrl -> webView.loadRequest(NSURLRequest.requestWithURL(nsUrl)) }
        },
    )
}
