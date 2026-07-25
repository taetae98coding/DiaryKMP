package io.github.taetae98coding.diary.compose.core.color

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public const val DIARY_COLOR_TITLE_INDICATOR_TEST_TAG: String = "DiaryColorTitleIndicator"

// 카드 머리글로 쓰는 색 표시와 제목 한 줄이다.
@Composable
public fun DiaryColorTitleRow(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiaryColorIndicator(
            color = color,
            modifier = Modifier.testTag(DIARY_COLOR_TITLE_INDICATOR_TEST_TAG),
        )
        Text(
            text = title,
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1,
            style = DiaryTheme.typography.titleMediumEmphasized,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryColorTitleRowPreview() {
    DiaryTheme {
        Surface {
            DiaryColorTitleRow(
                title = "제목",
                color = DiaryTheme.colorScheme.primary,
            )
        }
    }
}
