package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.UIKitView
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.web.WebPage
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<WebViewContent>())
internal class IosWebViewContent : WebViewContent {
    @Composable
    override fun UrlContent(
        url: String,
        modifier: Modifier,
    ) {
        UrlWebView(
            url = url,
            modifier = modifier,
        )
    }

    @Composable
    override fun PageContent(
        page: WebPage,
        modifier: Modifier,
    ) {
        PageWebView(
            page = page,
            modifier = modifier,
        )
    }
}

@Composable
private fun UrlWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        factory = { WKWebView() },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { webView ->
            NSURL.URLWithString(url)?.let { nsUrl -> webView.loadRequest(NSURLRequest.requestWithURL(nsUrl)) }
        },
    )
}

@Composable
private fun PageWebView(
    page: WebPage,
    modifier: Modifier,
) {
    UIKitView(
        factory = { WKWebView() },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { webView ->
            webView.loadHTMLString(page.body, NSURL.URLWithString(page.baseUrl))
        },
    )
}
