package io.github.taetae98coding.diary.feature.web.ui.detail.page

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webcookie.api.WebViewVisitedUrlRegistry
import org.koin.core.annotation.Single

private const val MIME_TYPE = "text/html"
private const val ENCODING = "UTF-8"

@Single(binds = [WebViewContent::class])
internal class AndroidWebViewContent : WebViewContent {
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
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { webView ->
            WebViewVisitedUrlRegistry.record(url = url)
            webView.loadUrl(url)
        },
    )
}

@Composable
private fun PageWebView(
    page: WebPage,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { webView ->
            webView.loadDataWithBaseURL(page.baseUrl, page.body, MIME_TYPE, ENCODING, null)
        },
    )
}
