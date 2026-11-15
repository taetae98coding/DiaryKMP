package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.icon.PersonRemoveIcon
import io.github.taetae98coding.diary.compose.core.icon.PhotoIcon
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItem
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.account.ui.Res
import io.github.taetae98coding.diary.feature.account.ui.profile_change_profile_image
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_account
import io.github.taetae98coding.diary.feature.account.ui.profile_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.account.ui.profile_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileScaffold(
    onEvent: (ProfileScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    deleteUiStateProvider: () -> ProfileDeleteUiState = { ProfileDeleteUiState() },
    isDeleteInProgressProvider: () -> Boolean = { false },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.profile_title),
                onNavigateUp = { onEvent(ProfileScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.profile_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        ProfileActionList(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            isInProgressProvider = isDeleteInProgressProvider,
        )
    }

    ProfileDeleteDialogHost(
        onEvent = onEvent,
        uiStateProvider = deleteUiStateProvider,
        isInProgressProvider = isDeleteInProgressProvider,
    )
}

@Composable
private fun ProfileActionList(
    onEvent: (ProfileScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    isInProgressProvider: () -> Boolean = { false },
) {
    val isEnabled = !isInProgressProvider()
    val errorColor = DiaryTheme.colorScheme.error
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .systemScroll(scrollState) { scrollState.animateScrollTo(0) }
                .verticalScroll(scrollState)
                .padding(DiaryTheme.dimens.screenPaddingValues),
        verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
    ) {
        DiarySegmentedListItem(
            onClick = { onEvent(ProfileScaffoldEvent.ClickChangeProfileImage) },
            enabled = isEnabled,
            index = 0,
            count = ACTION_COUNT,
            leadingContent = { PhotoIcon() },
        ) {
            Text(text = stringResource(Res.string.profile_change_profile_image))
        }

        DiarySegmentedListItem(
            onClick = { onEvent(ProfileScaffoldEvent.ClickDeleteAccount) },
            enabled = isEnabled,
            index = 1,
            count = ACTION_COUNT,
            leadingContent = {
                CompositionLocalProvider(LocalContentColor provides errorColor) {
                    PersonRemoveIcon()
                }
            },
        ) {
            Text(text = stringResource(Res.string.profile_delete_account), color = errorColor)
        }
    }
}

private const val ACTION_COUNT = 2

@ScreenPreview
@Composable
private fun ProfileScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        ProfileScaffold(
            onEvent = {},
            deleteUiStateProvider = { ProfileDeleteUiState(isConfirmVisible = isInProgress, isInProgress = isInProgress) },
            isDeleteInProgressProvider = { isInProgress },
        )
    }
}
