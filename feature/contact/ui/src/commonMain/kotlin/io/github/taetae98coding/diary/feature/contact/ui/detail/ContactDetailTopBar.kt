package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarActionScope
import io.github.taetae98coding.diary.compose.core.share.ShareAction
import io.github.taetae98coding.diary.compose.icon.Delete
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.icon.Star
import io.github.taetae98coding.diary.compose.icon.StarFill
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
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
    isShareSupported: Boolean = false,
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.contact_navigate_up_button_content_description)

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
                if (isShareSupported) {
                    ShareAction(onClick = { onEvent(ContactDetailScaffoldEvent.ClickShare) })
                }
                ContactDetailContentActions(onEvent = onEvent, uiState = uiState)
            }
        },
    )
}

@Composable
private fun DiaryTopBarActionScope.ContactDetailContentActions(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    uiState: ContactDetailUiState.Content,
) {
    val favoriteContentDescription = stringResource(Res.string.contact_detail_favorite_button_content_description)
    val unfavoriteContentDescription = stringResource(Res.string.contact_detail_unfavorite_button_content_description)
    val deleteContentDescription = stringResource(Res.string.contact_detail_delete_button_content_description)

    IconAction(
        icon = if (uiState.isFavorite) DiaryIcons.StarFill else DiaryIcons.Star,
        contentDescription = if (uiState.isFavorite) unfavoriteContentDescription else favoriteContentDescription,
        onClick = { onEvent(ContactDetailScaffoldEvent.ClickFavorite) },
        key = FAVORITE_ACTION_KEY,
        isInProgress = uiState.isFavoriteInProgress,
    )
    IconAction(
        icon = DiaryIcons.Delete,
        contentDescription = deleteContentDescription,
        onClick = { onEvent(ContactDetailScaffoldEvent.ClickDelete) },
        isInProgress = uiState.isDeleteInProgress,
    )
}

@ComponentPreview
@Composable
private fun ContactDetailTopBarPreview() {
    DiaryTheme {
        ContactDetailTopBar(
            onEvent = {},
            uiStateProvider = { ContactDetailUiState.Content(id = Uuid.NIL, detail = ContactDetail.EMPTY.copy(name = "김철수"), isFavorite = true) },
            isShareSupported = true,
        )
    }
}

private const val FAVORITE_ACTION_KEY = "favorite"
