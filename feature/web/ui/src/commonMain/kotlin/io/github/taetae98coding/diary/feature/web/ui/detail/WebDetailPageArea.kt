package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.previewWebPage
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPage
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailPageArea(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    pageUiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is WebDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            WebDetailPage(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                state = state,
                urlProvider = { uiStateProvider().urlOrEmpty() },
                uiStateProvider = pageUiStateProvider,
            )
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}

@ComponentPreview
@Composable
private fun WebDetailPageAreaPreview() {
    DiaryTheme {
        Surface {
            WebDetailPageArea(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
                pageUiStateProvider = { WebDetailPageUiState.Content(page = previewWebPage()) },
            )
        }
    }
}
