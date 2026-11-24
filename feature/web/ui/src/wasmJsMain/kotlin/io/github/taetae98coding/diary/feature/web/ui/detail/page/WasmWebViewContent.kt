@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.HtmlElementView
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.web.WebPage
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<WebViewContent>())
internal class WasmWebViewContent : WebViewContent {
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
    HtmlElementView(
        factory = {
            (document.createElement("iframe") as HTMLIFrameElement).apply {
                style.width = "100%"
                style.height = "100%"
                style.border = "none"
            }
        },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { element -> element.src = url },
    )
}

@Composable
private fun PageWebView(
    page: WebPage,
    modifier: Modifier,
) {
    HtmlElementView(
        factory = {
            (document.createElement("iframe") as HTMLIFrameElement).apply {
                style.width = "100%"
                style.height = "100%"
                style.border = "none"
            }
        },
        modifier = modifier.testTag(WEB_VIEW_TEST_TAG),
        update = { element -> element.srcdoc = page.body },
    )
}
