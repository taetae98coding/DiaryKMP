package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PlaylistHomeScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaylistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is PlaylistHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }
            }
        },
        modifier = modifier,
    )
}
