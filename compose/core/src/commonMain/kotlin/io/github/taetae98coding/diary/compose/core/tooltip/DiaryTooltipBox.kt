package io.github.taetae98coding.diary.compose.core.tooltip

import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun DiaryTooltipBox(
    text: String,
    modifier: Modifier = Modifier,
    anchorPosition: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    content: @Composable () -> Unit,
) {
    if (text.isEmpty()) {
        content()
    } else {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = anchorPosition),
            tooltip = { PlainTooltip { Text(text = text) } },
            state = rememberTooltipState(),
            modifier = modifier,
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryTooltipBoxPreview() {
    DiaryTheme {
        DiaryTooltipBox(text = "뒤로") {
            NavigateUpButton(onClick = {})
        }
    }
}
