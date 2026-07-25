@file:OptIn(ExperimentalFlexBoxApi::class)

package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignContent
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

private val Gap: Dp = 8.dp

@Composable
public fun DiaryFlexBox(
    modifier: Modifier = Modifier,
    content: @Composable LookaheadScope.() -> Unit,
) {
    LookaheadScope {
        val lookaheadScope = this

        FlexBox(
            modifier = modifier,
            config = {
                wrap(FlexWrap.Wrap)
                gap(Gap)
                justifyContent(FlexJustifyContent.Center)
                alignItems(FlexAlignItems.Center)
                // FlexBox가 줄 간격을 두 번 빼는 탓에 Center면 첫 줄이 줄 간격 합의 절반만큼 위로 밀리고 보고 높이도 그만큼 짧아진다.
                // 스크롤 영역 안에서는 밀린 만큼이 스크롤 위치 0보다 위에 남아 닿을 수 없으므로 Center를 쓰지 않는다.
                alignContent(FlexAlignContent.Start)
            },
        ) {
            lookaheadScope.content()
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryFlexBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryFlexBox {
                repeat(times = 3) { index ->
                    Text(text = "항목 ${'$'}index")
                }
            }
        }
    }
}
