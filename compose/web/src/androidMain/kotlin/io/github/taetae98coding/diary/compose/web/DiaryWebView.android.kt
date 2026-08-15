package io.github.taetae98coding.diary.compose.web

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import io.github.taetae98coding.diary.core.model.web.WebPage

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
                // 응답 본문 안의 스크립트를 실행해야 문서가 의도한 결과를 그대로 표시할 수 있다.
                settings.javaScriptEnabled = true
            }
        },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView ->
            webView.loadDataWithBaseURL(page.baseUrl, page.body, MIME_TYPE, ENCODING, null)
        },
    )
}

@Composable
public actual fun DiaryWebView(
    url: String,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // 응답 본문 안의 스크립트를 실행해야 문서가 의도한 결과를 그대로 표시할 수 있다.
                settings.javaScriptEnabled = true
            }
        },
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { webView -> webView.loadUrl(url) },
    )
}
