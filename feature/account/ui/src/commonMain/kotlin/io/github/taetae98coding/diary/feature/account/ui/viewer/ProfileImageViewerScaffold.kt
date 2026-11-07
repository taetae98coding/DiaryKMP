package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.account.ui.Res
import io.github.taetae98coding.diary.feature.account.ui.profile_image_viewer_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.account.ui.profile_image_viewer_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileImageViewerScaffold(
    onEvent: (ProfileImageViewerScaffoldEvent) -> Unit,
    imageUrl: String,
    modifier: Modifier = Modifier,
    zoomState: ProfileImageViewerZoomState = rememberProfileImageViewerZoomState(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.profile_image_viewer_title),
                onNavigateUp = { onEvent(ProfileImageViewerScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.profile_image_viewer_navigate_up_button_content_description),
            )
        },
    ) { paddingValues ->
        ProfileImageViewerImage(
            imageUrl = imageUrl,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            zoomState = zoomState,
        )
    }
}

@ScreenPreview
@Composable
private fun ProfileImageViewerScaffoldPreview() {
    DiaryTheme {
        ProfileImageViewerScaffold(
            onEvent = {},
            imageUrl = "https://example.com/profile.jpg",
        )
    }
}
