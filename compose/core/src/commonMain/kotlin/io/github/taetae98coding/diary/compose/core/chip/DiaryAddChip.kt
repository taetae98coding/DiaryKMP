package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryAddChip(
    onClick: () -> Unit,
    label: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
) {
    DiaryAssistChip(
        onClick = onClick,
        label = label,
        modifier =
            modifier.semantics {
                // 칩의 클릭 동작은 유지하고 이름만 덧붙이도록 action을 비워 둔다.
                onClick(label = actionLabel, action = null)
            },
        leadingIcon = { AddIcon() },
    )
}

@ComponentPreview
@Composable
private fun DiaryAddChipPreview() {
    DiaryTheme {
        Surface {
            DiaryAddChip(
                onClick = {},
                label = "태그 선택",
                actionLabel = "태그 선택하기",
            )
        }
    }
}
