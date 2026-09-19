@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.color

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.size
import androidx.compose.foundation.style.styleable
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
            modifier.styleable {
                size(8.dp)
                shape(CircleShape)
                background(color)
            },
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
