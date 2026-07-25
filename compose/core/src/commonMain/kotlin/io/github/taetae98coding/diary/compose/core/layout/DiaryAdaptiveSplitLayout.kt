package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryAdaptiveSplitLayout(
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    compactPrimaryWeight: Float = 1F,
    compactSecondaryWeight: Float = 1F,
) {
    if (isCompactWidth()) {
        Column(modifier = modifier) {
            Pane(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(compactPrimaryWeight),
                content = primary,
            )
            Pane(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(compactSecondaryWeight),
                content = secondary,
            )
        }
    } else {
        Row(modifier = modifier) {
            Pane(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(1F),
                content = primary,
            )
            Pane(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(1F),
                content = secondary,
            )
        }
    }
}

@Composable
private fun Pane(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        propagateMinConstraints = true,
    ) {
        content()
    }
}

@ComponentPreview
@Composable
private fun DiaryAdaptiveSplitLayoutPreview() {
    DiaryTheme {
        Surface {
            DiaryAdaptiveSplitLayout(
                primary = { Text(text = "주 영역") },
                secondary = { Text(text = "보조 영역") },
            )
        }
    }
}
