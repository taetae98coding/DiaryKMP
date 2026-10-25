package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.ListIcon
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_show_list_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_show_map_action
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoPlacePickerViewModeButton(
    modifier: Modifier = Modifier,
    state: MemoPlacePickerState = rememberMemoPlacePickerState(),
) {
    val contentDescription =
        when (state.viewMode) {
            MemoPlacePickerViewMode.LIST -> stringResource(Res.string.memo_place_picker_show_map_action)
            MemoPlacePickerViewMode.MAP -> stringResource(Res.string.memo_place_picker_show_list_action)
        }

    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = state::toggleViewMode,
            modifier = modifier,
        ) {
            DiaryCrossfade(targetState = state.viewMode) { viewMode ->
                when (viewMode) {
                    MemoPlacePickerViewMode.LIST -> MapIcon(contentDescription = contentDescription)
                    MemoPlacePickerViewMode.MAP -> ListIcon(contentDescription = contentDescription)
                }
            }
        }
    }
}

private class MemoPlacePickerViewModePreviewParameter : PreviewParameterProvider<MemoPlacePickerViewMode> {
    override val values: Sequence<MemoPlacePickerViewMode> = sequenceOf(MemoPlacePickerViewMode.LIST, MemoPlacePickerViewMode.MAP)
}

@ComponentPreview
@Composable
private fun MemoPlacePickerViewModeButtonPreview(
    @PreviewParameter(MemoPlacePickerViewModePreviewParameter::class) viewMode: MemoPlacePickerViewMode,
) {
    DiaryTheme {
        Surface {
            MemoPlacePickerViewModeButton(state = MemoPlacePickerState(initialViewMode = viewMode))
        }
    }
}
