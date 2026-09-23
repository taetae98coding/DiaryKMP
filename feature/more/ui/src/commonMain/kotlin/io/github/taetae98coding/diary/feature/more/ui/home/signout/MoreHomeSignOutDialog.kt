package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.more.ui.more_sign_out_cancel_button
import io.github.taetae98coding.diary.feature.more.ui.more_sign_out_confirm_button
import io.github.taetae98coding.diary.feature.more.ui.more_sign_out_confirm_message
import io.github.taetae98coding.diary.feature.more.ui.more_sign_out_confirm_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreHomeSignOutDialog(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { onEvent(MoreHomeScaffoldEvent.CancelSignOut) },
        confirmButton = {
            TextButton(onClick = { onEvent(MoreHomeScaffoldEvent.ConfirmSignOut) }) {
                Text(text = stringResource(Res.string.more_sign_out_confirm_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = { onEvent(MoreHomeScaffoldEvent.CancelSignOut) }) {
                Text(text = stringResource(Res.string.more_sign_out_cancel_button))
            }
        },
        title = { Text(text = stringResource(Res.string.more_sign_out_confirm_title)) },
        text = { Text(text = stringResource(Res.string.more_sign_out_confirm_message)) },
    )
}

@ComponentPreview
@Composable
private fun MoreHomeSignOutDialogPreview() {
    DiaryTheme {
        MoreHomeSignOutDialog(onEvent = {})
    }
}
