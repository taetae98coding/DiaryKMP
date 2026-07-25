package io.github.taetae98coding.diary.compose.core.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryColorIndicator(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape),
    )
}

@ComponentPreview
@Composable
private fun DiaryColorIndicatorPreview() {
    DiaryTheme {
        Surface {
            DiaryColorIndicator(color = Color(color = 0xFF3A7BD5.toInt()))
        }
    }
}
