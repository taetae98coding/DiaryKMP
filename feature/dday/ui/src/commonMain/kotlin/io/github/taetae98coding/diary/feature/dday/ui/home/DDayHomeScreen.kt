package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun DDayHomeScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DDayHomeScaffold(
        onEvent = { event ->
            when (event) {
                is DDayHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }
            }
        },
        modifier = modifier,
    )
}
