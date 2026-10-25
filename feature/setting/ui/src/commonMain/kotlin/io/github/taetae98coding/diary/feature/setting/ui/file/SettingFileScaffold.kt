package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingFileScaffold(
    onEvent: (SettingFileScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    uiStateProvider: () -> SettingFileUiState = { SettingFileUiState.Loading },
    componentVisibleProvider: () -> SettingFileScaffoldComponentVisible = { SettingFileScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_file_title),
                onNavigateUp = { onEvent(SettingFileScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            when (val uiState = uiStateProvider()) {
                is SettingFileUiState.Loading -> Unit

                is SettingFileUiState.Content -> {
                    item(key = DOWNLOAD_LOCATION_SECTION_KEY) {
                        SettingFileDownloadLocationSection(
                            onChooseClick = { onEvent(SettingFileScaffoldEvent.ClickChooseDirectory) },
                            onResetClick = { onEvent(SettingFileScaffoldEvent.ClickResetDirectory) },
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                            directory = uiState.directory,
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingFileScaffoldPreview() {
    DiaryTheme {
        SettingFileScaffold(
            onEvent = {},
            uiStateProvider = { SettingFileUiState.Content(directory = null) },
        )
    }
}

private const val DOWNLOAD_LOCATION_SECTION_KEY = "downloadLocationSection"
