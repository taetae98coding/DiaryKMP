package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public expect fun DiaryTopBar(
    modifier: Modifier = Modifier,
    title: DiaryTopBarTitle? = null,
    navigateUp: DiaryTopBarNavigateUp? = null,
    actions: List<DiaryTopBarAction> = emptyList(),
    isTitleCentered: Boolean = false,
)

@ComponentPreview
@Composable
private fun DiaryTopBarPreview() {
    DiaryTheme {
        DiaryTopBar(
            title = DiaryTopBarTitle.Text(text = "제목"),
            navigateUp = DiaryTopBarNavigateUp(contentDescription = "뒤로가기", onClick = {}),
            actions = listOf(DiaryTopBarAction.Icon(icon = DiaryTopBarIcon.SEARCH, contentDescription = "검색", onClick = {})),
        )
    }
}
