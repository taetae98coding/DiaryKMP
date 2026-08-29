package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ChecklistHomeScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChecklistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is ChecklistHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }
            }
        },
        modifier = modifier,
    )
}
