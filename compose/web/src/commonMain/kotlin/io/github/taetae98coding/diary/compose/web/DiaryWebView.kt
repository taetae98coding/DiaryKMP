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
