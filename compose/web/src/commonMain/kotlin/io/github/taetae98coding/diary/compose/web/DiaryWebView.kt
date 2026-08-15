package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.WebPage

public const val DIARY_WEB_VIEW_TEST_TAG: String = "DiaryWebView"

@Composable
public expect fun DiaryWebView(
    page: WebPage,
    modifier: Modifier = Modifier,
)

// 저장된 주소를 웹 표시 수단이 직접 열게 하므로 요청 헤더는 넘기지 않는다.
@Composable
public expect fun DiaryWebView(
    url: String,
    modifier: Modifier = Modifier,
)

@ComponentPreview
@Composable
private fun DiaryWebViewPagePreview() {
    DiaryTheme {
        DiaryWebView(page = previewWebPage())
    }
}

@ComponentPreview
@Composable
private fun DiaryWebViewUrlPreview() {
    DiaryTheme {
        DiaryWebView(url = previewWebPage().baseUrl)
    }
}
