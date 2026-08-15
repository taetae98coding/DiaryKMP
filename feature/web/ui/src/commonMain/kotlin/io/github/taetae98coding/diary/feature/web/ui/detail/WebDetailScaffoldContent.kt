package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.layout.isCompactWidth
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.previewWebPage
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailScaffoldContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    formState: WebFormState = rememberWebDetailFormState(),
    isChangedProvider: () -> Boolean = { false },
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    pageUiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider(),
        modifier = modifier,
        contentKey = { uiState -> uiState::class },
    ) { uiState ->
        when (uiState) {
            is WebDetailUiState.Loading -> DiaryLoadingBox(modifier = Modifier.fillMaxSize())

            is WebDetailUiState.Content ->
                if (isCompactWidth()) {
                    CompactContent(
                        onEvent = onEvent,
                        onFormEvent = onFormEvent,
                        state = state,
                        formState = formState,
                        isChangedProvider = isChangedProvider,
                        uiStateProvider = uiStateProvider,
                        pageUiStateProvider = pageUiStateProvider,
                        tagUiStateProvider = tagUiStateProvider,
                    )
                } else {
                    WideContent(
                        onEvent = onEvent,
                        onFormEvent = onFormEvent,
                        state = state,
                        formState = formState,
                        isChangedProvider = isChangedProvider,
                        uiStateProvider = uiStateProvider,
                        pageUiStateProvider = pageUiStateProvider,
                        tagUiStateProvider = tagUiStateProvider,
                    )
                }
        }
    }
}

@Composable
private fun CompactContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    state: WebDetailScaffoldState,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    pageUiStateProvider: () -> WebDetailPageUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WebDetailTabRow(
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )

        DiaryCrossfade(
            targetState = state.tab,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
        ) { tab ->
            when (tab) {
                WebDetailTab.FORM ->
                    WebDetailForm(
                        onEvent = onEvent,
                        onFormEvent = onFormEvent,
                        modifier = Modifier.fillMaxSize(),
                        state = formState,
                        isChangedProvider = isChangedProvider,
                        uiStateProvider = uiStateProvider,
                        tagUiStateProvider = tagUiStateProvider,
                    )

                WebDetailTab.PAGE ->
                    WebDetailPage(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        urlProvider = { uiStateProvider().urlOrEmpty() },
                        uiStateProvider = pageUiStateProvider,
                    )
            }
        }
    }
}

@Composable
private fun WideContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    state: WebDetailScaffoldState,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    pageUiStateProvider: () -> WebDetailPageUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        WebDetailForm(
            onEvent = onEvent,
            onFormEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F),
            state = formState,
            isChangedProvider = isChangedProvider,
            uiStateProvider = uiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
        )
        WebDetailPage(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F),
            state = state,
            urlProvider = { uiStateProvider().urlOrEmpty() },
            uiStateProvider = pageUiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun WebDetailScaffoldContentPreview() {
    DiaryTheme {
        Surface {
            WebDetailScaffoldContent(
                onEvent = {},
                onFormEvent = {},
                modifier = Modifier.fillMaxSize(),
                formState = rememberWebDetailFormState(initialDetail = previewWebDetail()),
                uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
                pageUiStateProvider = { WebDetailPageUiState.Content(page = previewWebPage()) },
            )
        }
    }
}
