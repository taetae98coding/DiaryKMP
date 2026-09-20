@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.color

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
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
        // 크기를 Style에 두면 intrinsic 측정에 보고되지 않아, 이 표시를 앞에 두는 칩이 이름에 줄 폭을 그만큼 덜 잡는다.
        modifier =
            modifier
                .size(8.dp)
                .styleable {
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
