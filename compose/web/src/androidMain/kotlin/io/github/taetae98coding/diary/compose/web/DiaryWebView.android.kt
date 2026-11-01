package io.github.taetae98coding.diary.compose.web

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webcookie.api.WebViewVisitedUrlRegistry

private const val MIME_TYPE = "text/html"
private const val ENCODING = "UTF-8"

@Composable
public actual fun DiaryWebView(
    page: WebPage,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Android WebView는 기본으로 스크립트를 실행하지 않고 사이트 저장 공간도 쓰지 않는다.
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView ->
            webView.loadDataWithBaseURL(page.baseUrl, page.body, MIME_TYPE, ENCODING, null)
        },
    )
}

@Composable
internal actual fun DiaryUrlWebView(
    url: String,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Android WebView는 기본으로 스크립트를 실행하지 않고 사이트 저장 공간도 쓰지 않는다.
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView ->
            WebViewVisitedUrlRegistry.record(url = url)
            webView.loadUrl(url)
        },
    )
}
