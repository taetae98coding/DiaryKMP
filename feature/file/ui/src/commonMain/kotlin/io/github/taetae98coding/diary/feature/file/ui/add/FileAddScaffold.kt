package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingUploadButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_add_title
import io.github.taetae98coding.diary.feature.file.ui.file_add_upload_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.file_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileAddScaffold(
    onEvent: (FileAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: FileAddFormState = rememberFileAddFormState(),
    uiStateProvider: () -> FileAddUiState = { FileAddUiState() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(FileAddScaffoldEvent.ClickUpload) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.file_add_title),
                onNavigateUp = { onEvent(FileAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.file_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingUploadButton(
                onClick = { onEvent(FileAddScaffoldEvent.ClickUpload) },
                contentDescription = stringResource(Res.string.file_add_upload_button_content_description),
                isInProgressProvider = { uiStateProvider().isUploading },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        FileAddForm(
            onChooseFileClick = { onEvent(FileAddScaffoldEvent.ClickChooseFile) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            selectedFileProvider = { uiStateProvider().selectedFile },
        )
    }
}

@ScreenPreview
@Composable
private fun FileAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isUploading: Boolean,
) {
    DiaryTheme {
        FileAddScaffold(
            onEvent = {},
            uiStateProvider = { FileAddUiState(isUploading = isUploading) },
        )
    }
}
