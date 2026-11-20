package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.UIKitView
import io.github.taetae98coding.diary.core.model.web.WebPage
import org.koin.core.annotation.Single
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Single(binds = [WebViewContent::class])
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
