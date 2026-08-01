package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_show_on_map_action
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoPlaceChip(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showOnMapActionLabel = stringResource(Res.string.memo_place_show_on_map_action)

    DiaryAssistChip(
        onClick = onClick,
        label = { Text(text = place.detail.title) },
        modifier =
            modifier.semantics {
                onClick(label = showOnMapActionLabel, action = null)
            },
        leadingIcon = { DiaryColorIndicator(color = place.detail.color.toColor()) },
    )
}

@ComponentPreview
@Composable
private fun MemoPlaceChipPreview() {
    val place = remember { previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780) }

    DiaryTheme {
        Surface {
            MemoPlaceChip(
                place = place,
                onClick = {},
            )
        }
    }
}
