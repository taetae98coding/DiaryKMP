@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor

@Composable
internal fun CalendarTimetableMemoBlock(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier =
            modifier
                .styleable {
                    shape(CalendarDefault.itemShape)
                    clip()
                    background(color)
                }.clickable(role = Role.Button, onClick = onClick)
                .padding(CalendarTimetableDefaults.MemoBlockContentPadding),
        color = color.contentColor(),
        overflow = TextOverflow.Ellipsis,
        style = DiaryTheme.typography.labelSmall,
    )
}

@ComponentPreview
@Composable
private fun CalendarTimetableMemoBlockPreview() {
    DiaryTheme {
        CalendarTimetableMemoBlock(
            title = "회의",
            color = Color(color = 0xFFE67E22.toInt()),
            onClick = {},
            modifier = Modifier.size(width = 96.dp, height = 48.dp),
        )
    }
}
