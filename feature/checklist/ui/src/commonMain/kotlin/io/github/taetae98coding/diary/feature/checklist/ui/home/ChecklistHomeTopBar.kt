package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_title
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistHomeTopBar(
    onEvent: (ChecklistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.checklist_home_title)) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(ChecklistHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.checklist_navigate_up_button_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun ChecklistHomeTopBarPreview() {
    DiaryTheme {
        ChecklistHomeTopBar(onEvent = {})
    }
}
