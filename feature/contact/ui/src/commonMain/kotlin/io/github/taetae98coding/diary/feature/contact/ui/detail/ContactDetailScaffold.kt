package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailScaffold(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactDetailFormState(),
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible = { ContactDetailScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? ContactDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(ContactDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            ContactDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            if (isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.contact_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? ContactDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        ContactDetailScaffoldContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun ContactDetailScaffoldPreview() {
    DiaryTheme {
        ContactDetailScaffold(
            onEvent = {},
            uiStateProvider = { ContactDetailUiState.Content(id = Uuid.NIL, detail = ContactDetail.EMPTY.copy(name = "김철수")) },
        )
    }
}
