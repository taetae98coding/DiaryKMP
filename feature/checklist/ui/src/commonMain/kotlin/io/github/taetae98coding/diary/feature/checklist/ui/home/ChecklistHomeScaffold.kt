package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_title
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistHomeScaffold(
    onEvent: (ChecklistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.checklist_home_title),
                onNavigateUp = { onEvent(ChecklistHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.checklist_navigate_up_button_content_description),
            )
        },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun ChecklistHomeScaffoldPreview() {
    DiaryTheme {
        ChecklistHomeScaffold(onEvent = {})
    }
}
