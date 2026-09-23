package io.github.taetae98coding.diary.compose.tag.filter

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.tag_filter_unselect_all_content_description
import io.github.taetae98coding.diary.compose.tag.tag_filter_unselect_all_label
import org.jetbrains.compose.resources.stringResource

@Composable
public fun TagFilterUnselectAllButton(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = false,
) {
    val unselectAllContentDescription = stringResource(Res.string.tag_filter_unselect_all_content_description)

    TextButton(
        onClick = { onEvent(TagFilterEvent.UnselectAll) },
        modifier =
            modifier.semantics {
                contentDescription = unselectAllContentDescription
            },
        enabled = isEnabled,
        shape = CircleShape,
    ) {
        Text(text = stringResource(Res.string.tag_filter_unselect_all_label))
    }
}

@ComponentPreview
@Composable
private fun TagFilterUnselectAllButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isEnabled: Boolean,
) {
    DiaryTheme {
        Surface {
            TagFilterUnselectAllButton(
                onEvent = {},
                isEnabled = isEnabled,
            )
        }
    }
}
