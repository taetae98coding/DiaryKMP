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
            Box(modifier = Modifier.width(PreviewWidth)) {
                DiaryChipLabel(label = "아주 긴 태그 이름")
            }
        }
    }
}

private val PreviewWidth = 80.dp
