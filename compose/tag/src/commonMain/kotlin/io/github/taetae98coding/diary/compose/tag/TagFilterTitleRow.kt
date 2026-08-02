package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlin.uuid.Uuid

@Composable
public fun TagFilterTitleRow(
    title: String,
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = DiaryTheme.typography.titleLargeEmphasized,
    selectedTagIdSetProvider: () -> Set<Uuid> = { emptySet() },
    isEnabledProvider: () -> Boolean = { true },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1F),
            style = titleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        TagFilterUnselectAllButton(
            onEvent = onEvent,
            isEnabled = isEnabledProvider() && selectedTagIdSetProvider().isNotEmpty(),
        )
    }
}

@ComponentPreview
@Composable
private fun TagFilterTitleRowPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasSelectedTag: Boolean,
) {
    DiaryTheme {
        Surface {
            TagFilterTitleRow(
                title = "태그 필터",
                onEvent = {},
                selectedTagIdSetProvider = { if (hasSelectedTag) setOf(Uuid.NIL) else emptySet() },
            )
        }
    }
}
