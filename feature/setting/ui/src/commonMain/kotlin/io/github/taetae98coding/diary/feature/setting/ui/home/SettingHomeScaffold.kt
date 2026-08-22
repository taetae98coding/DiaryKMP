package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
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
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar(onEvent = onEvent) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
        ) {
            itemsIndexed(
                items = settingHomeItemList,
                key = { _, item -> item.name },
            ) { index, item ->
                DiarySegmentedListItem(
                    onClick = { onEvent(SettingHomeScaffoldEvent.ClickItem(item)) },
                    modifier = Modifier.animateItem(),
                    index = index,
                    count = settingHomeItemList.size,
                ) {
                    Text(text = stringResource(item.labelResource))
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onEvent: (SettingHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.setting_home_title)) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(SettingHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
            )
        },
    )
}

@ScreenPreview
@Composable
private fun SettingHomeScaffoldPreview() {
    DiaryTheme {
        SettingHomeScaffold(onEvent = {})
    }
}
