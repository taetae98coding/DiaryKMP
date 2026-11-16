package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun DiaryTopBar(
    title: @Composable DiaryTopBarTitleScope.() -> Unit,
    modifier: Modifier = Modifier,
    isTitleCentered: Boolean = false,
    navigationIcon: @Composable DiaryTopBarNavigationIconScope.() -> Unit = {},
    actions: @Composable DiaryTopBarActionScope.() -> Unit = {},
) {
    val scope =
        DiaryTopBarOverrideScope(
            title = title,
            modifier = modifier,
            isTitleCentered = isTitleCentered,
            navigationIcon = navigationIcon,
            actions = actions,
        )

    with(LocalDiaryTopBarOverride.current) { scope.TopBar() }
}

@ComponentPreview
@Composable
private fun DiaryTopBarPreview() {
    DiaryTheme {
        DiaryTopBar(
            title = { Title(text = "제목") },
            navigationIcon = { NavigateUp(onClick = {}, contentDescription = "뒤로가기") },
            actions = { IconAction(icon = DiaryTopBarIcon.SEARCH, contentDescription = "검색", onClick = {}) },
        )
    }
}
