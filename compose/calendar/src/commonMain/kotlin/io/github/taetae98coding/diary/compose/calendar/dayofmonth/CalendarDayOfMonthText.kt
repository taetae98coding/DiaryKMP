@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.calendar.dayofmonth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlin.math.ceil
import kotlin.math.hypot

private const val CONTAINER_INDEX = 0
private const val TEXT_INDEX = 1

// 숫자를 감싸는 원형 배경의 지름은 숫자 크기에 따라 달라지므로 한 번의 measure pass 안에서 계산한다.
@Composable
internal fun CalendarDayOfMonthText(
    day: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            Spacer(
                modifier =
                    Modifier.styleable {
                        shape(CircleShape)
                        background(containerColor)
                    },
            )
            Text(
                text = day.toString(),
                color = contentColor,
                textAlign = TextAlign.Center,
                style = DiaryTheme.typography.labelSmallEmphasized,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val text =
            measurables[TEXT_INDEX].measure(
                constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity),
            )
        val diameter = ceil(hypot(x = text.width.toFloat(), y = text.height.toFloat())).toInt()
        val container = measurables[CONTAINER_INDEX].measure(Constraints.fixed(width = diameter, height = diameter))
        val width = constraints.constrainWidth(diameter)
        val height = constraints.constrainHeight(diameter)

        layout(width = width, height = height) {
            container.place(x = (width - container.width) / 2, y = (height - container.height) / 2)
            text.place(x = (width - text.width) / 2, y = (height - text.height) / 2)
        }
    }
}

@ComponentPreview
@Composable
private fun CalendarDayOfMonthTextPreview() {
    DiaryTheme {
        Surface {
            CalendarDayOfMonthText(
                day = 19,
                containerColor = DiaryTheme.colorScheme.primary,
                contentColor = DiaryTheme.colorScheme.onPrimary,
            )
        }
    }
}
