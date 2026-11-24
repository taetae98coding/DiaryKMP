package io.github.taetae98coding.diary.feature.web.ui.detail.page

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webcookie.api.WebViewVisitedUrlRegistry

private const val MIME_TYPE = "text/html"
private const val ENCODING = "UTF-8"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<WebViewContent>())
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
