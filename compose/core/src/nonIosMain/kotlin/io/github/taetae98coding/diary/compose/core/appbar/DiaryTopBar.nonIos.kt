package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public actual fun DiaryTopBar(
    title: @Composable DiaryTopBarTitleScope.() -> Unit,
    modifier: Modifier,
    isTitleCentered: Boolean,
    navigationIcon: @Composable DiaryTopBarNavigationIconScope.() -> Unit,
    actions: @Composable DiaryTopBarActionScope.() -> Unit,
) {
    MaterialDiaryTopBar(
        title = title,
        modifier = modifier,
        isTitleCentered = isTitleCentered,
        navigationIcon = navigationIcon,
        actions = actions,
    )
}
