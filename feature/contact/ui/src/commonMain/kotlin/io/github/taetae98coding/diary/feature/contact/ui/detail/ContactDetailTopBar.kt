package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_favorite_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_unfavorite_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailTopBar(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible = { ContactDetailScaffoldComponentVisible() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.contact_navigate_up_button_content_description)
    val favoriteContentDescription = stringResource(Res.string.contact_detail_favorite_button_content_description)
    val unfavoriteContentDescription = stringResource(Res.string.contact_detail_unfavorite_button_content_description)
    val deleteContentDescription = stringResource(Res.string.contact_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is ContactDetailUiState.Content) {
                Title(text = uiState.detail.name, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is ContactDetailUiState.Content) {
                IconAction(
                    icon = if (uiState.isFavorite) DiaryTopBarIcon.STAR_FILL else DiaryTopBarIcon.STAR,
                    contentDescription = if (uiState.isFavorite) unfavoriteContentDescription else favoriteContentDescription,
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickFavorite) },
                    isInProgress = uiState.isFavoriteInProgress,
                )
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun ContactDetailTopBarPreview() {
    DiaryTheme {
        ContactDetailTopBar(
            onEvent = {},
            uiStateProvider = { ContactDetailUiState.Content(id = Uuid.NIL, detail = ContactDetail.EMPTY.copy(name = "김철수"), isFavorite = true) },
        )
    }
}
