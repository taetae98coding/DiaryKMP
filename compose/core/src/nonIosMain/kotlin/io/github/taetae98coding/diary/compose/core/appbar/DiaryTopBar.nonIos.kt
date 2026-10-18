package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public actual fun DiaryTopBar(
    modifier: Modifier,
    title: DiaryTopBarTitle?,
    navigateUp: DiaryTopBarNavigateUp?,
    actions: List<DiaryTopBarAction>,
    isTitleCentered: Boolean,
) {
    MaterialDiaryTopBar(
        modifier = modifier,
        title = title,
        navigateUp = navigateUp,
        actions = actions,
        isTitleCentered = isTitleCentered,
    )
}
