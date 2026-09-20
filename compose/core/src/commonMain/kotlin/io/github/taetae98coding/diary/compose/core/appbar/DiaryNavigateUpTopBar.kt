package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            if (isNavigateUpVisibleProvider()) {
                NavigateUpButton(
                    onClick = onNavigateUp,
                    contentDescription = navigateUpContentDescription,
                )
            }
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
