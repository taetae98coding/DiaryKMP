package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.icon.PhotoIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_apply_button_content_description
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_choose_photo_button
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_title
import org.jetbrains.compose.resources.stringResource

// docs/design/profile-image-edit.md `편집 영역`이 정한 편집 영역의 최대 한 변.
private val EditorMaxSize: Dp = 400.dp

@Composable
internal fun ProfileImageEditScaffold(
    onEvent: (ProfileImageEditScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ProfileImageEditState = rememberProfileImageEditState(),
    uiStateProvider: () -> ProfileImageEditUiState = { ProfileImageEditUiState() },
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier =
            modifier.submitShortcut(isEnabledProvider = { state.isReady && !uiStateProvider().isInProgress }) {
                onEvent(ProfileImageEditScaffoldEvent.ClickApply)
            },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.more_profile_image_edit_title),
                onNavigateUp = { onEvent(ProfileImageEditScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.more_profile_image_edit_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = hostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = state.isReady) {
                FloatingCheckButton(
                    onClick = {
                        if (!uiStateProvider().isInProgress) {
                            onEvent(ProfileImageEditScaffoldEvent.ClickApply)
                        }
                    },
                    contentDescription = stringResource(Res.string.more_profile_image_edit_apply_button_content_description),
                    isInProgressProvider = { uiStateProvider().isInProgress },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        ProfileImageEditContent(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
        )
    }
}

@Composable
private fun ProfileImageEditContent(
    onEvent: (ProfileImageEditScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ProfileImageEditState = rememberProfileImageEditState(),
    uiStateProvider: () -> ProfileImageEditUiState = { ProfileImageEditUiState() },
) {
    val dimens = DiaryTheme.dimens

    BoxWithConstraints(
        modifier = modifier.padding(dimens.screenPaddingValues),
        contentAlignment = Alignment.Center,
    ) {
        // 편집 영역은 폭과, 사진 선택 버튼을 뺀 높이 중 짧은 쪽을 한 변으로 하고 최대 한 변을 넘지 않는다.
        val editorSize =
            minOf(maxWidth, maxHeight - ButtonDefaults.MinHeight - dimens.componentSpacing, EditorMaxSize)
                .coerceAtLeast(0.dp)

        Column(
            modifier = Modifier.width(editorSize),
            verticalArrangement = Arrangement.spacedBy(dimens.componentSpacing),
        ) {
            ProfileImageEditor(
                modifier = Modifier.size(editorSize),
                state = state,
                isEnabledProvider = { !uiStateProvider().isInProgress },
            )
            ChoosePhotoButton(
                onClick = { onEvent(ProfileImageEditScaffoldEvent.ClickChoosePhoto) },
                modifier = Modifier.fillMaxWidth(),
                isEnabledProvider = { !uiStateProvider().isInProgress },
            )
        }
    }
}

@Composable
private fun ChoosePhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabledProvider: () -> Boolean = { true },
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = isEnabledProvider(),
    ) {
        PhotoIcon(modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(Res.string.more_profile_image_edit_choose_photo_button))
    }
}

@ScreenPreview
@Composable
private fun ProfileImageEditScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        ProfileImageEditScaffold(
            onEvent = {},
            state = ProfileImageEditState(initialUri = FileUri("preview"), initialPhoto = ProfileImageEditPhoto.Ready(width = 400, height = 200)),
            uiStateProvider = { ProfileImageEditUiState(isInProgress = isInProgress) },
        )
    }
}
