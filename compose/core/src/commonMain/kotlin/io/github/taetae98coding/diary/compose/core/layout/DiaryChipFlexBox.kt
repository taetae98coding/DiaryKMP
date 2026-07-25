package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

/**
 * 담는 자리가 높이를 정하지 않는 곳에 두는 칩 영역이다. 칩 영역 높이를 최소로 삼고 칩이 늘면 그만큼 커진다.
 */
@Composable
public fun DiaryChipFlexBox(
    modifier: Modifier = Modifier,
    content: @Composable LookaheadScope.() -> Unit,
) {
    ChipFlexBox(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = DiaryTheme.dimens.chipAreaHeight),
        content = content,
    )
}

/**
 * 담는 자리가 높이를 정하는 곳에 두는 칩 영역이다. 칩이 늘어도 칩 영역 높이를 지키고 영역 안에서 세로로 스크롤한다.
 *
 * 세로로 스크롤하는 자리에는 두지 않는다. 같은 방향 스크롤이 겹치면 칩 위를 잡았을 때 바깥이 움직이지 않는다.
 */
@Composable
public fun DiaryScrollableChipFlexBox(
    modifier: Modifier = Modifier,
    content: @Composable LookaheadScope.() -> Unit,
) {
    ChipFlexBox(
        modifier =
            modifier
                .fillMaxWidth()
                .height(DiaryTheme.dimens.chipAreaHeight)
                .verticalScroll(rememberScrollState()),
        content = content,
    )
}

@Composable
private fun ChipFlexBox(
    modifier: Modifier,
    content: @Composable LookaheadScope.() -> Unit,
) {
    // 여백은 스크롤 안쪽에 두어 칩과 함께 움직이게 한다. 스크롤 바깥에 두면 첫 줄 위와 마지막 줄 아래에 여백이 남지 않는다.
    DiaryFlexBox(
        modifier = modifier.padding(DiaryTheme.dimens.screenPaddingValues),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun DiaryChipFlexBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryChipFlexBox {
                repeat(times = 3) { index ->
                    Text(text = "칩 $index")
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryScrollableChipFlexBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryScrollableChipFlexBox {
                repeat(times = 12) { index ->
                    Text(text = "칩 $index")
                }
            }
        }
    }
}
