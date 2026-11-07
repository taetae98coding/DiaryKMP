package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ProfileImageViewerScreen(
    navigateUp: () -> Unit,
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    ProfileImageViewerScaffold(
        onEvent = { event ->
            when (event) {
                is ProfileImageViewerScaffoldEvent.ClickNavigateUp -> navigateUp()
            }
        },
        imageUrl = imageUrl,
        modifier = modifier,
    )
}
