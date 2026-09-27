package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_add_file_not_selected_message
import io.github.taetae98coding.diary.feature.file.ui.file_add_file_unreadable_message
import io.github.taetae98coding.diary.feature.file.ui.file_add_title_blank_message
import io.github.taetae98coding.diary.feature.file.ui.file_add_upload_succeeded_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_too_large_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileAddScreenEffect(
    effect: Flow<FileAddEffect> = emptyFlow(),
    state: FileAddFormState = rememberFileAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val titleBlankMessage = stringResource(Res.string.file_add_title_blank_message)
    val fileNotSelectedMessage = stringResource(Res.string.file_add_file_not_selected_message)
    val fileUnreadableMessage = stringResource(Res.string.file_add_file_unreadable_message)
    val tooLargeMessage = stringResource(Res.string.file_home_upload_too_large_message)
    val succeededMessage = stringResource(Res.string.file_add_upload_succeeded_message)
    val failedMessage = stringResource(Res.string.file_home_upload_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is FileAddEffect.UploadStarted -> {
                state.clear()
                state.titleState.requestFocus()
            }

            is FileAddEffect.TitleBlank -> {
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = titleBlankMessage) }
            }

            is FileAddEffect.FileNotSelected -> {
                coroutineScope.launch { state.hostState.showImmediate(message = fileNotSelectedMessage) }
            }

            is FileAddEffect.FileUnreadable -> {
                coroutineScope.launch { state.hostState.showImmediate(message = fileUnreadableMessage) }
            }

            is FileAddEffect.FileTooLarge -> {
                coroutineScope.launch { state.hostState.showImmediate(message = tooLargeMessage) }
            }

            is FileAddEffect.UploadSucceeded -> {
                coroutineScope.launch { state.hostState.showImmediate(message = succeededMessage) }
            }

            is FileAddEffect.UploadFailed -> {
                coroutineScope.launch { state.hostState.showImmediate(message = failedMessage) }
            }
        }
    }
}
