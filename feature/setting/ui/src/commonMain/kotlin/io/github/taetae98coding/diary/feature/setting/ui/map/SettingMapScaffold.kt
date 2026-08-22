package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_map_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingMapScaffold(
    onEvent: (SettingMapScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> SettingMapUiState = { SettingMapUiState.Loading },
    componentVisibleProvider: () -> SettingMapScaffoldComponentVisible = {
        SettingMapScaffoldComponentVisible()
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
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
                is SettingMapUiState.Loading -> Unit

                is SettingMapUiState.Loaded -> {
                    item(key = DEFAULT_PROVIDER_GROUP_KEY) {
                        SettingMapDefaultProviderSection(
                            defaultProvider = uiState.defaultProvider,
                            onSelect = { provider -> onEvent(SettingMapScaffoldEvent.SelectDefaultProvider(provider = provider)) },
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onEvent: (SettingMapScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentVisibleProvider: () -> SettingMapScaffoldComponentVisible = { SettingMapScaffoldComponentVisible() },
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.setting_map_title)) },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(SettingMapScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                )
            }
        },
    )
}

@ScreenPreview
@Composable
private fun SettingMapScaffoldPreview() {
    DiaryTheme {
        SettingMapScaffold(
            uiStateProvider = { SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER) },
            onEvent = {},
        )
    }
}

private const val DEFAULT_PROVIDER_GROUP_KEY = "defaultProviderGroup"
