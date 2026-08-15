@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.HtmlElementView
import io.github.taetae98coding.diary.core.model.web.WebPage
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

@Composable
public actual fun DiaryWebView(
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
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { element -> element.srcdoc = page.body },
    )
}

@Composable
public actual fun DiaryWebView(
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
        modifier = modifier.testTag(DIARY_WEB_VIEW_TEST_TAG),
        update = { element -> element.src = url },
    )
}
