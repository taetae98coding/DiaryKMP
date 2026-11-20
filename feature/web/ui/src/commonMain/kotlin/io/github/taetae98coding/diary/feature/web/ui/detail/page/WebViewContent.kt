package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.core.model.web.WebPage

internal const val WEB_VIEW_TEST_TAG: String = "WebView"

internal interface WebViewContent {
    @Composable
    fun UrlContent(
        url: String,
        modifier: Modifier,
    )

    @Composable
    fun PageContent(
        page: WebPage,
        modifier: Modifier,
    )
}
