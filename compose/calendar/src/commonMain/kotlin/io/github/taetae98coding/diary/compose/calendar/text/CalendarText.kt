package io.github.taetae98coding.diary.compose.calendar.text

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor

@Composable
public fun CalendarText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Text(
        text = text,
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color = color)
                .basicMarquee(iterations = Int.MAX_VALUE)
                .padding(1.dp),
        color = color.contentColor(),
        maxLines = 1,
        style = DiaryTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
    )
}

@ComponentPreview
@Composable
private fun CalendarTextPreview() {
    DiaryTheme {
        CalendarText(text = "제헌절")
    }
}
