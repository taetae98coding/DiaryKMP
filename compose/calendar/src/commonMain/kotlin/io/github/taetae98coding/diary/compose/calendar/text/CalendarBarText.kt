package io.github.taetae98coding.diary.compose.calendar.text

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal const val CALENDAR_BAR_TEXT_BAR_TEST_TAG = "CalendarBarTextBar"

@Composable
public fun CalendarBarText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Row(
        modifier =
            modifier
                .padding(1.dp)
                .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Spacer(
            modifier =
                Modifier
                    .testTag(CALENDAR_BAR_TEXT_BAR_TEST_TAG)
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color = color),
        )
        Text(
            text = text,
            modifier =
                Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1,
            style = DiaryTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@ComponentPreview
@Composable
private fun CalendarBarTextPreview() {
    DiaryTheme {
        CalendarBarText(text = "회의 준비")
    }
}
