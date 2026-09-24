package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_add_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebDetailMemoFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingAddButton(
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(Res.string.web_detail_memo_add_button_content_description),
    )
}

@ComponentPreview
@Composable
private fun WebDetailMemoFloatingActionButtonPreview() {
    DiaryTheme {
        Surface {
            WebDetailMemoFloatingActionButton(onClick = {})
        }
    }
}
