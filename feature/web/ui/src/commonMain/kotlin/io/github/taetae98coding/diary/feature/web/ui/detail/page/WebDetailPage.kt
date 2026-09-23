package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.DiaryWebView
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.rememberWebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewModeBar
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.previewWebPage
import io.github.taetae98coding.diary.feature.web.ui.web_detail_page_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_page_failure_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_page_failure_title
import io.github.taetae98coding.diary.feature.web.ui.web_detail_page_retry_button
import org.jetbrains.compose.resources.stringResource

internal const val WEB_DETAIL_PAGE_FAILURE_TEST_TAG: String = "WebDetailPageFailure"

@Composable
internal fun WebDetailPage(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    urlProvider: () -> String = { "" },
    uiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
) {
    Column(modifier = modifier) {
        WebDetailViewModeBar(
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )

        DiaryCrossfade(
            targetState = state.viewMode,
            modifier = Modifier.fillMaxSize(),
        ) { mode ->
            when (mode) {
                WebDetailViewMode.URL -> UrlWebView(url = urlProvider(), modifier = Modifier.fillMaxSize())

                WebDetailViewMode.RESPONSE ->
                    ResponsePage(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        uiStateProvider = uiStateProvider,
                    )
            }
        }
    }
}

@Composable
private fun UrlWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.web_detail_page_content_description)

    DiaryWebView(
        url = url,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    )
}

@Composable
private fun ResponsePage(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
) {
    DiaryCrossfade(
        targetState = uiStateProvider(),
        modifier = modifier,
        contentKey = { uiState -> uiState::class },
    ) { uiState ->
        when (uiState) {
            is WebDetailPageUiState.Loading -> DiaryLoadingBox(modifier = Modifier.fillMaxSize())

            is WebDetailPageUiState.Content -> {
                val contentDescription = stringResource(Res.string.web_detail_page_content_description)

                DiaryWebView(
                    page = uiState.page,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .semantics { this.contentDescription = contentDescription },
                )
            }

            is WebDetailPageUiState.Failure -> FailureBox(onEvent = onEvent, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun FailureBox(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(DiaryTheme.dimens.screenPaddingValues)
                .testTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        ) {
            CompositionLocalProvider(LocalContentColor provides DiaryTheme.colorScheme.onSurfaceVariant) {
                WebIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize))
                Text(
                    text = stringResource(Res.string.web_detail_page_failure_title),
                    style = DiaryTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.web_detail_page_failure_description),
                    style = DiaryTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Button(onClick = { onEvent(WebDetailScaffoldEvent.ClickRetry) }) {
                Text(text = stringResource(Res.string.web_detail_page_retry_button))
            }
        }
    }
}

private data class WebDetailPagePreviewValue(
    val viewMode: WebDetailViewMode,
    val uiState: WebDetailPageUiState,
)

private class WebDetailPagePreviewParameter : PreviewParameterProvider<WebDetailPagePreviewValue> {
    override val values: Sequence<WebDetailPagePreviewValue> =
        sequenceOf(
            WebDetailPagePreviewValue(viewMode = WebDetailViewMode.URL, uiState = WebDetailPageUiState.Loading),
            WebDetailPagePreviewValue(viewMode = WebDetailViewMode.RESPONSE, uiState = WebDetailPageUiState.Loading),
            WebDetailPagePreviewValue(viewMode = WebDetailViewMode.RESPONSE, uiState = WebDetailPageUiState.Content(page = previewWebPage())),
            WebDetailPagePreviewValue(viewMode = WebDetailViewMode.RESPONSE, uiState = WebDetailPageUiState.Failure),
        )
}

@ComponentPreview
@Composable
private fun WebDetailPagePreview(
    @PreviewParameter(WebDetailPagePreviewParameter::class) value: WebDetailPagePreviewValue,
) {
    DiaryTheme {
        Surface {
            WebDetailPage(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                state = rememberWebDetailScaffoldState(initialViewMode = value.viewMode),
                urlProvider = { previewWebDetail().url },
                uiStateProvider = { value.uiState },
            )
        }
    }
}
