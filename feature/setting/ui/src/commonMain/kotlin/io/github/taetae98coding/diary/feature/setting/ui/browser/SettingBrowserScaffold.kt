package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewChromeProfileList
import io.github.taetae98coding.diary.feature.setting.ui.setting_browser_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingBrowserScaffold(
    onEvent: (SettingBrowserScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> SettingBrowserUiState = { SettingBrowserUiState.Loading },
    componentVisibleProvider: () -> SettingBrowserScaffoldComponentVisible = {
        SettingBrowserScaffoldComponentVisible()
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_browser_title),
                onNavigateUp = { onEvent(SettingBrowserScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
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
                is SettingBrowserUiState.Loading -> Unit

                is SettingBrowserUiState.Loaded -> {
                    item(key = CHROME_PROFILE_GROUP_KEY) {
                        SettingBrowserChromeProfileSection(
                            onSelect = { directory -> onEvent(SettingBrowserScaffoldEvent.SelectProfile(directory = directory)) },
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                            profileList = uiState.profileList,
                            selectedProfileDirectory = uiState.selectedProfileDirectory,
                            isProfileListUnavailable = uiState.isProfileListUnavailable,
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingBrowserScaffoldPreview() {
    DiaryTheme {
        SettingBrowserScaffold(
            onEvent = {},
            uiStateProvider = {
                SettingBrowserUiState.Loaded(
                    profileList = previewChromeProfileList(),
                    selectedProfileDirectory = previewChromeProfileList().first().directory,
                )
            },
        )
    }
}

private const val CHROME_PROFILE_GROUP_KEY = "chromeProfileGroup"
