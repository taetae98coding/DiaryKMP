package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.AutoAwesomeIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.memo_gemini_button_content_description)

    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            AutoAwesomeIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun MemoGeminiButtonPreview() {
    DiaryTheme {
        Surface {
            MemoGeminiButton(onClick = {})
        }
    }
}
