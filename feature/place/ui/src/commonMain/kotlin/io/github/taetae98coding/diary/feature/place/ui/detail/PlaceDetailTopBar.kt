package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarAction
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarNavigateUp
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarTitle
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.feature.place.ui.PREVIEW_PLACE_DETAIL
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_detail_open_google_map_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_detail_open_naver_map_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_search_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaceDetailTopBar(
    onEvent: (PlaceDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PlaceDetailUiState = { PlaceDetailUiState.Loading },
    mapProviderProvider: () -> DiaryMapProvider = { DiaryMapProvider.NAVER },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.place_navigate_up_button_content_description)
    val openExternalMapContentDescription = mapProviderProvider().openExternalMapContentDescription()
    val searchContentDescription = stringResource(Res.string.place_search_button_content_description)
    val deleteContentDescription = stringResource(Res.string.place_detail_delete_button_content_description)

    DiaryTopBar(
        modifier = modifier,
        title =
            if (uiState is PlaceDetailUiState.Content) {
                DiaryTopBarTitle.Text(text = uiState.detail.title, isMarquee = true)
            } else {
                null
            },
        navigateUp =
            DiaryTopBarNavigateUp(
                contentDescription = navigateUpContentDescription,
                onClick = { onEvent(PlaceDetailScaffoldEvent.ClickNavigateUp) },
            ),
        actions =
            buildList {
                if (uiState is PlaceDetailUiState.Content) {
                    add(
                        DiaryTopBarAction.Icon(
                            icon = DiaryTopBarIcon.OPEN_IN_NEW,
                            contentDescription = openExternalMapContentDescription,
                            onClick = { onEvent(PlaceDetailScaffoldEvent.ClickOpenExternalMap) },
                        ),
                    )
                    if (uiState.isMapDisplayed) {
                        add(
                            DiaryTopBarAction.Icon(
                                icon = DiaryTopBarIcon.SEARCH,
                                contentDescription = searchContentDescription,
                                onClick = { onEvent(PlaceDetailScaffoldEvent.ClickSearch) },
                            ),
                        )
                    }
                    add(
                        DiaryTopBarAction.Icon(
                            icon = DiaryTopBarIcon.DELETE,
                            contentDescription = deleteContentDescription,
                            onClick = { onEvent(PlaceDetailScaffoldEvent.ClickDelete) },
                            isInProgress = uiState.isDeleteInProgress,
                        ),
                    )
                }
            },
    )
}

@Composable
private fun DiaryMapProvider.openExternalMapContentDescription(): String =
    when (this) {
        DiaryMapProvider.NAVER -> stringResource(Res.string.place_detail_open_naver_map_button_content_description)
        DiaryMapProvider.GOOGLE -> stringResource(Res.string.place_detail_open_google_map_button_content_description)
    }

@ComponentPreview
@Composable
private fun PlaceDetailTopBarPreview() {
    DiaryTheme {
        PlaceDetailTopBar(
            onEvent = {},
            uiStateProvider = {
                PlaceDetailUiState.Content(
                    id = Uuid.NIL,
                    detail = PREVIEW_PLACE_DETAIL,
                )
            },
        )
    }
}
