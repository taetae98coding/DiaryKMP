package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

@Composable
internal fun MemoPlacePickerRow(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    place: Place? = null,
    isSelected: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isSelected,
        onSelectedChange = {
            if (isSelected) {
                place?.let { value -> onEvent(MemoPlacePickerEvent.Unselect(id = value.id)) }
            } else {
                place?.let { value -> onEvent(MemoPlacePickerEvent.Select(id = value.id)) }
            }
        },
        color = place?.detail?.color?.toColor() ?: Color.Transparent,
        label = place?.detail?.title.orEmpty(),
        modifier = modifier,
        enabled = place != null,
    )
}

@ComponentPreview
@Composable
private fun MemoPlacePickerRowPreview() {
    val place = remember { previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780) }

    DiaryTheme {
        Surface {
            MemoPlacePickerRow(
                onEvent = {},
                place = place,
                isSelected = true,
            )
        }
    }
}
