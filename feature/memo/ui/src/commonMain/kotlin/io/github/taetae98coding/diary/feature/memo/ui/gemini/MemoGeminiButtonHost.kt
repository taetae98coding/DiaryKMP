package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MemoGeminiButtonHost(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisibleProvider: () -> Boolean = { false },
) {
    if (!isVisibleProvider()) return

    MemoGeminiButton(
        onClick = onClick,
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun MemoGeminiButtonHostPreview() {
    DiaryTheme {
        Surface {
            MemoGeminiButtonHost(
                onClick = {},
                isVisibleProvider = { true },
            )
        }
    }
}
