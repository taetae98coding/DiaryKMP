package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.text.CalendarBarText
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo

@Composable
internal fun CalendarHomeMemoText(
    memo: CalendarMemo,
    modifier: Modifier = Modifier,
) {
    if (memo.dateTime.isSingleDayDateTime()) {
        CalendarBarText(
            text = memo.title,
            modifier = modifier,
            color = Color(color = memo.color.toInt()),
        )
    } else {
        CalendarText(
            text = memo.title,
            modifier = modifier,
            color = Color(color = memo.color.toInt()),
        )
    }
}

@ComponentPreview
@Composable
private fun CalendarHomeMemoTextPreview() {
    val memo = remember { previewCalendarMemo() }

    DiaryTheme {
        Surface {
            CalendarHomeMemoText(memo = memo)
        }
    }
}
