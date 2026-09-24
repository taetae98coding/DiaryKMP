package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItem
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_home_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHomeScaffold(
    onEvent: (SettingHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> SettingHomeUiState = { SettingHomeUiState.Loading },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_home_title),
                onNavigateUp = { onEvent(SettingHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
        ) {
            when (val uiState = uiStateProvider()) {
                is SettingHomeUiState.Loading -> Unit

                is SettingHomeUiState.Loaded -> {
                    itemsIndexed(
                        items = uiState.itemList,
                        key = { _, item -> item.name },
                    ) { index, item ->
                        DiarySegmentedListItem(
                            onClick = { onEvent(SettingHomeScaffoldEvent.ClickItem(item)) },
                            modifier = Modifier.animateItem(),
                            index = index,
                            count = uiState.itemList.size,
                        ) {
                            Text(text = stringResource(item.labelResource))
                        }
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingHomeScaffoldPreview() {
    DiaryTheme {
        SettingHomeScaffold(
            onEvent = {},
            uiStateProvider = { SettingHomeUiState.Loaded(itemList = settingHomeItemList) },
        )
    }
}
