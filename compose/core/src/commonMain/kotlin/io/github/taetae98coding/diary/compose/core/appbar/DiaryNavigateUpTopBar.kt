package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryNavigateUpTopBar(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    navigateUpContentDescription: String? = null,
    isNavigateUpVisibleProvider: () -> Boolean = { true },
    actions: List<DiaryTopBarAction> = emptyList(),
) {
    DiaryTopBar(
        modifier = modifier,
        title = DiaryTopBarTitle.Text(text = title),
        navigateUp =
            if (isNavigateUpVisibleProvider()) {
                DiaryTopBarNavigateUp(contentDescription = navigateUpContentDescription, onClick = onNavigateUp)
            } else {
                null
            },
        actions = actions,
    )
}

@ComponentPreview
@Composable
private fun DiaryNavigateUpTopBarPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isNavigateUpVisible: Boolean,
) {
    DiaryTheme {
        DiaryNavigateUpTopBar(
            title = "제목",
            onNavigateUp = {},
            isNavigateUpVisibleProvider = { isNavigateUpVisible },
        )
    }
}
