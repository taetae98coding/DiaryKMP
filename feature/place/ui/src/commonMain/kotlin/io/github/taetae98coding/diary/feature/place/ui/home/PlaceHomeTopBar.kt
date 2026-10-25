package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewModePreviewParameter
import io.github.taetae98coding.diary.feature.place.ui.place_home_list_view_mode_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_home_map_view_mode_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_home_search_action_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_home_title
import io.github.taetae98coding.diary.feature.place.ui.place_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceHomeTopBar(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: PlaceHomeScaffoldState = rememberPlaceHomeScaffoldState(),
) {
    val title = stringResource(Res.string.place_home_title)
    val navigateUpContentDescription = stringResource(Res.string.place_navigate_up_button_content_description)
    val searchContentDescription = stringResource(Res.string.place_home_search_action_content_description)
    val listViewModeContentDescription = stringResource(Res.string.place_home_list_view_mode_button_content_description)
    val mapViewModeContentDescription = stringResource(Res.string.place_home_map_view_mode_button_content_description)
    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(PlaceHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
        actions = {
            IconAction(
                icon = DiaryTopBarIcon.SEARCH,
                contentDescription = searchContentDescription,
                onClick = { onEvent(PlaceHomeScaffoldEvent.ClickSearch) },
            )
            when (state.viewMode) {
                PlaceHomeViewMode.MAP ->
                    IconAction(
                        icon = DiaryTopBarIcon.LIST,
                        contentDescription = listViewModeContentDescription,
                        onClick = state::toggleViewMode,
                    )

                PlaceHomeViewMode.LIST ->
                    IconAction(
                        icon = DiaryTopBarIcon.MAP,
                        contentDescription = mapViewModeContentDescription,
                        onClick = state::toggleViewMode,
                    )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun PlaceHomeTopBarPreview(
    @PreviewParameter(PlaceHomeViewModePreviewParameter::class) viewMode: PlaceHomeViewMode,
) {
    DiaryTheme {
        PlaceHomeTopBar(
            onEvent = {},
            state = PlaceHomeScaffoldState(initialViewMode = viewMode),
        )
    }
}
