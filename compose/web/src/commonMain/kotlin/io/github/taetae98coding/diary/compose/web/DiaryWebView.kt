package io.github.taetae98coding.diary.compose.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
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
public fun DiaryWebView(
    url: String,
    onSessionImportFailed: () -> Unit,
    modifier: Modifier = Modifier,
    sessionImportFailureState: DiaryWebSessionImportFailureState = rememberDiaryWebSessionImportFailureState(),
) {
    val session = LocalDiaryWebSession.current

    SessionImportFailedEffect(
        failureId = session.failureId,
        onSessionImportFailed = onSessionImportFailed,
        state = sessionImportFailureState,
    )

    DiaryCrossfade(
        targetState = session.importCount.takeUnless { session.isPreparing },
        modifier = modifier,
    ) { importCount ->
        if (importCount == null) {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        } else {
            DiaryUrlWebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal expect fun DiaryUrlWebView(
    url: String,
    modifier: Modifier = Modifier,
)

@Composable
private fun SessionImportFailedEffect(
    failureId: Int?,
    onSessionImportFailed: () -> Unit,
    state: DiaryWebSessionImportFailureState,
) {
    val latestOnSessionImportFailed by rememberUpdatedState(onSessionImportFailed)

    LaunchedEffect(failureId, state) {
        if (failureId != null && !state.isNotified(failureId)) {
            state.markNotified(failureId)
            latestOnSessionImportFailed()
        }
    }
}

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
        DiaryWebView(
            url = previewWebPage().baseUrl,
            onSessionImportFailed = {},
        )
    }
}
