package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_add_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_title
import io.github.taetae98coding.diary.feature.contact.ui.contact_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactForm
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactAddFormState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactAddScaffold(
    onEvent: (ContactAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactAddFormState(),
    uiStateProvider: () -> ContactAddUiState = { ContactAddUiState() },
    componentVisibleProvider: () -> ContactAddScaffoldComponentVisible = { ContactAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(ContactAddScaffoldEvent.ClickAdd) },
        topBar = {
            TopBar(
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(ContactAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.contact_add_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        ContactForm(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
        )
    }
}

@Composable
private fun TopBar(
    onEvent: (ContactAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentVisibleProvider: () -> ContactAddScaffoldComponentVisible = { ContactAddScaffoldComponentVisible() },
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.contact_add_title)) },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(ContactAddScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.contact_navigate_up_button_content_description),
                )
            }
        },
    )
}

@ScreenPreview
@Composable
private fun ContactAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        ContactAddScaffold(
            onEvent = {},
            uiStateProvider = { ContactAddUiState(isInProgress = isInProgress) },
        )
    }
}
