package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.OpenInNewButton
import io.github.taetae98coding.diary.compose.core.button.SearchButton
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

    TopAppBar(
        title = {
            if (uiState is PlaceDetailUiState.Content) {
                Text(
                    text = uiState.detail.title,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(PlaceDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.place_navigate_up_button_content_description),
            )
        },
        actions = {
            if (uiState is PlaceDetailUiState.Content) {
                OpenInNewButton(
                    onClick = { onEvent(PlaceDetailScaffoldEvent.ClickOpenExternalMap) },
                    contentDescription = mapProviderProvider().openExternalMapContentDescription(),
                )
                SearchButton(
                    onClick = { onEvent(PlaceDetailScaffoldEvent.ClickSearch) },
                    contentDescription = stringResource(Res.string.place_search_button_content_description),
                )
                DeleteButton(
                    onClick = { onEvent(PlaceDetailScaffoldEvent.ClickDelete) },
                    contentDescription = stringResource(Res.string.place_detail_delete_button_content_description),
                    isInProgressProvider = { uiState.isDeleteInProgress },
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
