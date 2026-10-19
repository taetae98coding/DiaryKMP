package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_detail_download_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.file_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileDetailTopBar(
    onEvent: (FileDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> FileDetailUiState = { FileDetailUiState.Loading },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.file_navigate_up_button_content_description)
    val downloadContentDescription = stringResource(Res.string.file_detail_download_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is FileDetailUiState.Content) {
                Title(text = uiState.file.title)
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(FileDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
        actions = {
            if (uiState is FileDetailUiState.Content) {
                IconAction(
                    icon = DiaryTopBarIcon.DOWNLOAD,
                    contentDescription = downloadContentDescription,
                    onClick = { onEvent(FileDetailScaffoldEvent.ClickDownload) },
                    isInProgress = uiState.isDownloadInProgress,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun FileDetailTopBarPreview() {
    DiaryTheme {
        FileDetailTopBar(
            onEvent = {},
            uiStateProvider = { FileDetailUiState.Content(file = previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679)) },
        )
    }
}
