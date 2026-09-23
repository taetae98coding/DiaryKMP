package io.github.taetae98coding.diary.compose.tag.filter

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.chip.DiaryFilterChip
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

@Composable
public fun TagFilterChip(
    tag: Tag,
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
) {
    val color = tag.detail.color.toColor()
    val defaultColors = FilterChipDefaults.filterChipColors()

    DiaryFilterChip(
        selected = isSelected,
        onClick = {
            if (isSelected) {
                onEvent(TagFilterEvent.Unselect(id = tag.id))
            } else {
                onEvent(TagFilterEvent.Select(id = tag.id))
            }
        },
        label = tag.detail.emojiWithTitle,
        modifier = modifier,
        enabled = isEnabled,
        leadingIcon = {
            if (!isSelected) {
                DiaryColorIndicator(color = color)
            }
        },
        // Material 기본 비활성 색을 덮어 태그 컬러를 유지한다. 흐림은 칩이 아니라 태그 필터 영역이 맡는다.
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = color,
                selectedLabelColor = color.contentColor(),
                disabledContainerColor = defaultColors.containerColor,
                disabledLabelColor = if (isSelected) color.contentColor() else defaultColors.labelColor,
                disabledSelectedContainerColor = color,
            ),
    )
}

@ComponentPreview
@Composable
private fun TagFilterChipPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isSelected: Boolean,
) {
    DiaryTheme {
        Surface {
            TagFilterChip(
                tag = previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                onEvent = {},
                isSelected = isSelected,
            )
        }
    }
}
