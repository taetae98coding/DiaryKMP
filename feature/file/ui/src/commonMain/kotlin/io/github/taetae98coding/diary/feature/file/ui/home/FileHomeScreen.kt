package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun FileHomeScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FileHomeScaffold(
        onEvent = { event ->
            when (event) {
                is FileHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }
            }
        },
        modifier = modifier,
    )
}
