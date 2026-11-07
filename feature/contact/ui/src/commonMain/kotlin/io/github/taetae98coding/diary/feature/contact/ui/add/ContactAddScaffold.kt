package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_add_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_title
import io.github.taetae98coding.diary.feature.contact.ui.contact_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactForm
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormEvent
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactAddFormState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactAddScaffold(
    onEvent: (ContactAddScaffoldEvent) -> Unit,
    onFormEvent: (ContactFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> ContactAddUiState = { ContactAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> ContactAddScaffoldComponentVisible = { ContactAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(ContactAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.contact_add_title),
                onNavigateUp = { onEvent(ContactAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.contact_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
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
            onEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            tagUiStateProvider = tagUiStateProvider,
        )
    }

    EntityTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
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
            onFormEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { ContactAddUiState(isInProgress = isInProgress) },
        )
    }
}
