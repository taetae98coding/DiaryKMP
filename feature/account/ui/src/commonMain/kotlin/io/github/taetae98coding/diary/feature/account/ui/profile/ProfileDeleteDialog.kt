package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties
import io.github.taetae98coding.diary.compose.core.icon.WarningIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.account.ui.Res
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_cancel_button
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_confirm_button
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_confirm_message
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_confirm_title
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_progress_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileDeleteDialogHost(
    onEvent: (ProfileScaffoldEvent) -> Unit,
    uiStateProvider: () -> ProfileDeleteUiState = { ProfileDeleteUiState() },
    isInProgressProvider: () -> Boolean = { false },
) {
    if (!uiStateProvider().isConfirmVisible) return

    ProfileDeleteDialog(
        onEvent = onEvent,
        isInProgress = isInProgressProvider(),
    )
}

@Composable
internal fun ProfileDeleteDialog(
    onEvent: (ProfileScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    isInProgress: Boolean = false,
) {
    val errorColor = DiaryTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = { onEvent(ProfileScaffoldEvent.CancelDeleteAccount) },
        confirmButton = {
            TextButton(
                onClick = { onEvent(ProfileScaffoldEvent.ConfirmDeleteAccount) },
                enabled = !isInProgress,
            ) {
                // 진행 중에도 버튼 크기를 그대로 두어 취소 버튼과 정렬과 자리를 유지한다.
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.profile_delete_confirm_button),
                        modifier = Modifier.alpha(if (isInProgress) 0F else 1F),
                        color = errorColor,
                    )
                    if (isInProgress) {
                        DeleteProgressIndicator()
                    }
                }
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = { onEvent(ProfileScaffoldEvent.CancelDeleteAccount) },
                enabled = !isInProgress,
            ) {
                Text(text = stringResource(Res.string.profile_delete_cancel_button))
            }
        },
        icon = { WarningIcon() },
        title = { Text(text = stringResource(Res.string.profile_delete_confirm_title)) },
        text = { Text(text = stringResource(Res.string.profile_delete_confirm_message)) },
        iconContentColor = errorColor,
        properties = DialogProperties(dismissOnBackPress = !isInProgress, dismissOnClickOutside = !isInProgress),
    )
}

@Composable
private fun DeleteProgressIndicator(modifier: Modifier = Modifier) {
    val contentDescription = stringResource(Res.string.profile_delete_progress_content_description)

    CircularWavyProgressIndicator(
        modifier =
            modifier
                .size(DiaryTheme.dimens.inProgressIndicatorSize)
                .semantics { this.contentDescription = contentDescription },
    )
}

@ComponentPreview
@Composable
private fun ProfileDeleteDialogPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        ProfileDeleteDialog(onEvent = {}, isInProgress = isInProgress)
    }
}
