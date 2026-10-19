package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_add_title_blank_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_download_directory_unavailable_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_download_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_download_succeeded_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_name_blank_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_name_too_long_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_update_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileDetailScreenEffect(
    effect: Flow<FileDetailEffect>,
    state: FileDetailFormState,
) {
    val coroutineScope = rememberCoroutineScope()
    val titleBlankMessage = stringResource(Res.string.file_add_title_blank_message)
    val nameBlankMessage = stringResource(Res.string.file_detail_name_blank_message)
    val nameTooLongMessage = stringResource(Res.string.file_detail_name_too_long_message)
    val updateSucceededMessage = stringResource(Res.string.file_detail_update_succeeded_message)
    val updateFailedMessage = stringResource(Res.string.file_detail_update_failed_message)
    val directoryUnavailableMessage = stringResource(Res.string.file_detail_download_directory_unavailable_message)
    val downloadFailedMessage = stringResource(Res.string.file_detail_download_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is FileDetailEffect.TitleBlank -> {
                state.titleState.requestFocus()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = titleBlankMessage) }
            }

            is FileDetailEffect.NameBlank -> {
                state.requestNameFocus()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = nameBlankMessage) }
            }

            is FileDetailEffect.NameTooLong -> {
                state.requestNameFocus()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = nameTooLongMessage) }
            }

            is FileDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is FileDetailEffect.UpdateFailed -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateFailedMessage) }
            }

            is FileDetailEffect.DownloadSucceeded -> {
                coroutineScope.launch {
                    state.snackbarHostState.showImmediate(message = getString(Res.string.file_detail_download_succeeded_message, value.name))
                }
            }

            is FileDetailEffect.DownloadDirectoryUnavailable -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = directoryUnavailableMessage) }
            }

            is FileDetailEffect.DownloadFailed -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = downloadFailedMessage) }
            }
        }
    }
}
