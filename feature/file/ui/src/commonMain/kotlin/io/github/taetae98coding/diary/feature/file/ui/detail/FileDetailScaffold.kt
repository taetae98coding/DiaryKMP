package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file.FileLoadFailed
import io.github.taetae98coding.diary.feature.file.ui.file_detail_load_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_loading_content_description
import io.github.taetae98coding.diary.feature.file.ui.file_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileDetailScaffold(
    onEvent: (FileDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: FileDetailFormState = rememberFileDetailFormState(),
    uiStateProvider: () -> FileDetailUiState = { FileDetailUiState.Loading },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? FileDetailUiState.Content
            content != null && state.detail != content.file.toDetail()
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(FileDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            FileDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(FileDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.file_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? FileDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        FileDetailScaffoldContent(
            onRetry = { onEvent(FileDetailScaffoldEvent.ClickRetry) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
            isUpdateButtonVisibleProvider = { isChanged },
        )
    }
}

@Composable
private fun FileDetailScaffoldContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    state: FileDetailFormState = rememberFileDetailFormState(),
    uiStateProvider: () -> FileDetailUiState = { FileDetailUiState.Loading },
    isUpdateButtonVisibleProvider: () -> Boolean = { false },
) {
    val uiState = uiStateProvider()

    DiaryCrossfade(
        targetState = uiState.toBody(),
        modifier = modifier,
    ) { body ->
        when (body) {
            FileDetailBody.LOADING -> {
                DiaryLoadingBox(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = stringResource(Res.string.file_detail_loading_content_description),
                )
            }

            FileDetailBody.FAILED -> {
                FileLoadFailed(
                    message = stringResource(Res.string.file_detail_load_failed_message),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            FileDetailBody.CONTENT -> {
                (uiStateProvider() as? FileDetailUiState.Content)?.let { content ->
                    FileDetailForm(
                        file = content.file,
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        isUpdateButtonVisibleProvider = isUpdateButtonVisibleProvider,
                    )
                }
            }
        }
    }
}

private enum class FileDetailBody {
    LOADING,
    FAILED,
    CONTENT,
}

private fun FileDetailUiState.toBody(): FileDetailBody =
    when (this) {
        is FileDetailUiState.Loading -> FileDetailBody.LOADING
        is FileDetailUiState.Failed -> FileDetailBody.FAILED
        is FileDetailUiState.Content -> FileDetailBody.CONTENT
    }

internal fun DiaryFile.toDetail(): DiaryFileDetail =
    DiaryFileDetail(
        name = name,
        title = title,
        description = description,
    )

private class FileDetailUiStatePreviewParameter : PreviewParameterProvider<FileDetailUiState> {
    override val values: Sequence<FileDetailUiState> =
        sequenceOf(
            FileDetailUiState.Loading,
            FileDetailUiState.Failed,
            FileDetailUiState.Content(file = previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679)),
        )
}

@ScreenPreview
@Composable
private fun FileDetailScaffoldPreview(
    @PreviewParameter(FileDetailUiStatePreviewParameter::class) uiState: FileDetailUiState,
) {
    val detail = (uiState as? FileDetailUiState.Content)?.file?.toDetail() ?: DiaryFileDetail(name = "", title = "", description = "")

    DiaryTheme {
        FileDetailScaffold(
            onEvent = {},
            state = rememberFileDetailFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
