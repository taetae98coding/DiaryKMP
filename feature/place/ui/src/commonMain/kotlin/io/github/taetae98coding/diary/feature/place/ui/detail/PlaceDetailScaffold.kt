package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.place.ui.PREVIEW_PLACE_DETAIL
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.ReflectCoordinateEffect
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.place_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchHost
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchUiState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaceDetailScaffold(
    state: PlaceFormState,
    onEvent: (PlaceDetailScaffoldEvent) -> Unit,
    onFormEvent: (PlaceFormEvent) -> Unit,
    onSearchEvent: (PlaceSearchEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PlaceDetailUiState = { PlaceDetailUiState.Loading },
    searchUiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    ReflectCoordinateEffect(state = state)

    PlaceSearchHost(
        onEvent = onSearchEvent,
        uiStateProvider = searchUiStateProvider,
        dialogState = state.searchDialogState,
        mapState = state.mapState,
    )

    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? PlaceDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(PlaceDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            PlaceDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                mapProviderProvider = { state.mapState.provider },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(PlaceDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.place_detail_update_button_content_description),
                    isInProgressProvider = {
                        (uiStateProvider() as? PlaceDetailUiState.Content)?.isUpdateInProgress == true
                    },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        PlaceDetailScaffoldContent(
            onFormEvent = onFormEvent,
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            uiStateProvider = uiStateProvider,
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

private class PlaceDetailUiStatePreviewParameter : PreviewParameterProvider<PlaceDetailUiState> {
    override val values: Sequence<PlaceDetailUiState> =
        sequenceOf(
            PlaceDetailUiState.Loading,
            PlaceDetailUiState.Content(
                id = Uuid.NIL,
                detail = PREVIEW_PLACE_DETAIL,
            ),
        )
}

@ScreenPreview
@Composable
private fun PlaceDetailScaffoldPreview(
    @PreviewParameter(PlaceDetailUiStatePreviewParameter::class) uiState: PlaceDetailUiState,
) {
    DiaryTheme {
        PlaceDetailScaffold(
            state = rememberPlaceDetailFormState(initialDetail = PREVIEW_PLACE_DETAIL),
            onEvent = {},
            onFormEvent = {},
            onSearchEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { uiState },
        )
    }
}
