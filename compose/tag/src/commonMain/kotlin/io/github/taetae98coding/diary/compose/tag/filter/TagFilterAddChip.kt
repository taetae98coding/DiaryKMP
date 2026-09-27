package io.github.taetae98coding.diary.compose.tag.filter

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.tag_filter_add_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagFilterAddChip(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val label = stringResource(Res.string.tag_filter_add_label)
    val defaultColors = AssistChipDefaults.assistChipColors()

    DiaryAddChip(
        onClick = { onEvent(TagFilterEvent.ClickAdd) },
        label = label,
        actionLabel = label,
        modifier = modifier,
        enabled = isEnabled,
        colors =
            AssistChipDefaults.assistChipColors(
                disabledLabelColor = defaultColors.labelColor,
                disabledLeadingIconContentColor = defaultColors.leadingIconContentColor,
            ),
    )
}

@ComponentPreview
@Composable
private fun TagFilterAddChipPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isEnabled: Boolean,
) {
    DiaryTheme {
        Surface {
            TagFilterAddChip(
                onEvent = {},
                isEnabled = isEnabled,
            )
        }
    }
}
