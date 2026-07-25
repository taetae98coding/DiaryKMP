package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryInputColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(scrollState)
                .padding(DiaryTheme.dimens.screenPaddingValues),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun DiaryInputColumnPreview() {
    DiaryTheme {
        Surface {
            DiaryInputColumn {
                repeat(times = 3) { index ->
                    Text(text = "입력 ${'$'}index")
                }
            }
        }
    }
}
