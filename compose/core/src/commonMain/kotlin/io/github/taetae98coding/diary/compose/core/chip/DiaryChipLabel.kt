package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

/**
 * 칩 이름 표시 디자인(docs/design/chip.md)이 정한 한 줄 유지와 끝 생략을 모든 칩에 같게 적용한다.
 *
 * 생략해도 화면 낭독 도구에는 전체 이름이 그대로 전달된다.
 */
@Composable
internal fun DiaryChipLabel(label: String) {
    Text(
        text = label,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@ComponentPreview
@Composable
private fun DiaryChipLabelPreview() {
    DiaryTheme {
        Surface {
            // 생략이 일어나는 모습을 보려면 이름보다 좁은 폭으로 감싸야 한다.
            Box(modifier = Modifier.width(PreviewWidth)) {
                DiaryChipLabel(label = "아주 긴 태그 이름")
            }
        }
    }
}

private val PreviewWidth = 80.dp
