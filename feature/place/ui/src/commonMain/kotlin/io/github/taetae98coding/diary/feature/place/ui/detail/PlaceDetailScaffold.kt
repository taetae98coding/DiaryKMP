package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.place.ui.PREVIEW_PLACE_DETAIL
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoFloatingActionButton
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoTab
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTabRow
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTabState
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.rememberPlaceDetailTabState
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
    onSearchEvent: (PlaceSearchEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tabState: PlaceDetailTabState = rememberPlaceDetailTabState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PlaceDetailUiState = { PlaceDetailUiState.Loading },
    searchUiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    tabFloatingActionButton: @Composable (PlaceDetailTab) -> Unit,
    tabContent: @Composable (PlaceDetailTab) -> Unit,
) {
    ReflectCoordinateEffect(state = state)

    PlaceSearchHost(
        onEvent = onSearchEvent,
        uiStateProvider = searchUiStateProvider,
        dialogState = state.searchDialogState,
        mapState = state.mapState,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            PlaceDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                mapProviderProvider = { state.mapState.provider },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = { tabFloatingActionButton(tabState.tab) },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            PlaceDetailTabRow(
                modifier = Modifier.fillMaxWidth(),
                state = tabState,
            )
            PlaceDetailPager(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
                state = tabState,
                tabContent = tabContent,
            )
        }
    }

    EntityTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@Composable
internal fun PlaceDetailUpdateFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryScaleVisibility(
        visible = isVisible,
        modifier = modifier,
    ) {
        FloatingCheckButton(
            onClick = onClick,
            contentDescription = stringResource(Res.string.place_detail_update_button_content_description),
            isInProgressProvider = isInProgressProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun PlaceDetailUpdateFloatingActionButtonPreview() {
    DiaryTheme {
        PlaceDetailUpdateFloatingActionButton(onClick = {}, isVisible = true)
    }
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
    val state = rememberPlaceDetailFormState(initialDetail = PREVIEW_PLACE_DETAIL)

    DiaryTheme {
        PlaceDetailScaffold(
            state = state,
            onEvent = {},
            onSearchEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { uiState },
            tabFloatingActionButton = { tab ->
                when (tab) {
                    PlaceDetailTab.DETAIL -> PlaceDetailUpdateFloatingActionButton(onClick = {}, isVisible = true)
                    PlaceDetailTab.MEMO -> PlaceDetailMemoFloatingActionButton(onClick = {})
                }
            },
        ) { tab ->
            when (tab) {
                PlaceDetailTab.DETAIL ->
                    PlaceDetailScaffoldContent(
                        onFormEvent = {},
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        uiStateProvider = { uiState },
                    )

                PlaceDetailTab.MEMO -> PlaceDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
