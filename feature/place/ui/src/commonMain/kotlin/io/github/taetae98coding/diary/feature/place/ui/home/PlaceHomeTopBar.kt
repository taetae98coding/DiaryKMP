package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.SearchButton
import io.github.taetae98coding.diary.compose.core.icon.ListIcon
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
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
    TopAppBar(
        title = { Text(text = stringResource(Res.string.place_home_title)) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(PlaceHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.place_navigate_up_button_content_description),
            )
        },
        actions = {
            SearchButton(
                onClick = { onEvent(PlaceHomeScaffoldEvent.ClickSearch) },
                contentDescription = stringResource(Res.string.place_home_search_action_content_description),
            )
            ViewModeButton(state = state)
        },
    )
}

@Composable
private fun ViewModeButton(state: PlaceHomeScaffoldState) {
    val viewMode = state.viewMode
    val contentDescription =
        when (viewMode) {
            PlaceHomeViewMode.MAP -> stringResource(Res.string.place_home_list_view_mode_button_content_description)
            PlaceHomeViewMode.LIST -> stringResource(Res.string.place_home_map_view_mode_button_content_description)
        }

    DiaryTooltipBox(text = contentDescription) {
        IconButton(onClick = state::toggleViewMode) {
            DiaryCrossfade(targetState = viewMode) { targetViewMode ->
                when (targetViewMode) {
                    PlaceHomeViewMode.MAP ->
                        ListIcon(
                            contentDescription = stringResource(Res.string.place_home_list_view_mode_button_content_description),
                        )

                    PlaceHomeViewMode.LIST ->
                        MapIcon(
                            contentDescription = stringResource(Res.string.place_home_map_view_mode_button_content_description),
                        )
                }
            }
        }
    }
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
