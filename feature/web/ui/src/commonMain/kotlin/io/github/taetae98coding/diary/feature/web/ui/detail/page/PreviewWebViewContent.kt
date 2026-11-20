package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.core.model.web.WebPage

internal object PreviewWebViewContent : WebViewContent {
    @Composable
    override fun UrlContent(
        url: String,
        modifier: Modifier,
    ) {
        Box(modifier = modifier)
    }

    @Composable
    override fun PageContent(
        page: WebPage,
        modifier: Modifier,
    ) {
        Box(modifier = modifier)
    }
}
