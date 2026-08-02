package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.ListIcon
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_list_view_mode_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_map_view_mode_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailPlaceViewModeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModeProvider: () -> TagDetailPlaceViewMode = { TagDetailPlaceViewMode.LIST },
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        DiaryCrossfade(targetState = viewModeProvider()) { viewMode ->
            when (viewMode) {
                TagDetailPlaceViewMode.LIST ->
                    MapIcon(
                        contentDescription = stringResource(Res.string.tag_detail_place_map_view_mode_button_content_description),
                    )

                TagDetailPlaceViewMode.MAP ->
                    ListIcon(
                        contentDescription = stringResource(Res.string.tag_detail_place_list_view_mode_button_content_description),
                    )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun TagDetailPlaceViewModeButtonPreview(
    @PreviewParameter(TagDetailPlaceViewModePreviewParameter::class) viewMode: TagDetailPlaceViewMode,
) {
    DiaryTheme {
        Surface {
            TagDetailPlaceViewModeButton(
                onClick = {},
                viewModeProvider = { viewMode },
            )
        }
    }
}
