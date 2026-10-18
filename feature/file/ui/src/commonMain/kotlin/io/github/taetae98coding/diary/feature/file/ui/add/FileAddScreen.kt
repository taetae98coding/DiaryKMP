package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker

@Composable
internal fun FileAddScreen(
    navigateUp: () -> Unit,
    filePicker: FilePicker,
    uploadViewModel: FileAddViewModel,
    accountViewModel: FileAddAccountViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberFileAddFormState()
    val uiState by uploadViewModel.uiState.collectAsStateWithLifecycle()

    DiaryTitleInputFocusEffect(state = state.titleState)
    FileAddViewingEffect(uploadViewModel = uploadViewModel)
    FileAddScreenEffect(
        effect = uploadViewModel.effect,
        state = state,
    )
    FileAddAccountEffect(
        accountViewModel = accountViewModel,
        navigateUp = navigateUp,
    )

    FileAddScaffold(
        onEvent = { event ->
            when (event) {
                is FileAddScaffoldEvent.ClickNavigateUp -> navigateUp()
                is FileAddScaffoldEvent.ClickUpload -> uploadViewModel.upload(title = state.title, description = state.description)
                is FileAddScaffoldEvent.ClickChooseFile -> filePicker.open()
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
    )
}

@Composable
private fun FileAddViewingEffect(uploadViewModel: FileAddViewModel) {
    LifecycleStartEffect(uploadViewModel) {
        uploadViewModel.startViewing()

        onStopOrDispose { uploadViewModel.stopViewing() }
    }
}

@Composable
private fun FileAddAccountEffect(
    accountViewModel: FileAddAccountViewModel,
    navigateUp: () -> Unit,
) {
    CollectEffect(accountViewModel.uiState) { uiState ->
        if (uiState.isGuest) navigateUp()
    }
}
