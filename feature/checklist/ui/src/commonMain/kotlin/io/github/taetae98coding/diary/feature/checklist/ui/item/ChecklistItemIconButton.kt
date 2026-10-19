package io.github.taetae98coding.diary.feature.checklist.ui.item

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.icon.RemoveIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
internal fun ChecklistItemAddButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isEnabledProvider: () -> Boolean = { true },
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            // 진행 표시로 아이콘이 바뀌어도 버튼의 이름이 사라지지 않도록 이름은 버튼에 둔다.
            modifier = modifier.semantics { this.contentDescription = contentDescription },
            enabled = isEnabledProvider(),
        ) {
            DiaryCrossfade(targetState = isInProgressProvider()) { isInProgress ->
                if (isInProgress) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(DiaryTheme.dimens.inProgressIndicatorSize))
                } else {
                    AddIcon()
                }
            }
        }
    }
}

@Composable
internal fun ChecklistItemRemoveButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            RemoveIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun ChecklistItemAddButtonPreview() {
    DiaryTheme {
        ChecklistItemAddButton(
            onClick = {},
            contentDescription = "항목 넣기",
        )
    }
}

@ComponentPreview
@Composable
private fun ChecklistItemRemoveButtonPreview() {
    DiaryTheme {
        ChecklistItemRemoveButton(
            onClick = {},
            contentDescription = "항목 빼기",
        )
    }
}
