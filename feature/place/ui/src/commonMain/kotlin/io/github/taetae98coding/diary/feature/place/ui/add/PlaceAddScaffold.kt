package io.github.taetae98coding.diary.feature.place.ui.add

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
import io.github.taetae98coding.diary.compose.core.button.SearchButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceForm
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.ReflectCoordinateEffect
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.place_add_add_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_add_title
import io.github.taetae98coding.diary.feature.place.ui.place_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_search_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchHost
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchUiState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceAddScaffold(
    onEvent: (PlaceAddScaffoldEvent) -> Unit,
    onFormEvent: (PlaceFormEvent) -> Unit,
    onSearchEvent: (PlaceSearchEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchUiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
    state: PlaceFormState = rememberPlaceAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PlaceAddUiState = { PlaceAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    ReflectCoordinateEffect(state = state)

    PlaceSearchHost(
        onEvent = onSearchEvent,
        uiStateProvider = searchUiStateProvider,
        dialogState = state.searchDialogState,
        mapState = state.mapState,
    )

    Scaffold(
        modifier = modifier.submitShortcut { onEvent(PlaceAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.place_add_title),
                onNavigateUp = { onEvent(PlaceAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.place_navigate_up_button_content_description),
                actions = {
                    if (uiStateProvider().defaultProvider != null) {
                        SearchButton(
                            onClick = { onEvent(PlaceAddScaffoldEvent.ClickSearch) },
                            contentDescription = stringResource(Res.string.place_search_button_content_description),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(PlaceAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.place_add_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        PlaceForm(
            onEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            isMapDisplayed = uiStateProvider().defaultProvider != null,
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
private fun PlaceAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        PlaceAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onSearchEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { PlaceAddUiState(isInProgress = isInProgress) },
        )
    }
}
