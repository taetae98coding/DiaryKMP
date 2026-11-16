package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_add_button_content_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailAddFloatingActionButton(
    contentDescription: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingAddButton(
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(contentDescription),
    )
}

@ComponentPreview
@Composable
private fun TagDetailAddFloatingActionButtonPreview() {
    DiaryTheme {
        Surface {
            TagDetailAddFloatingActionButton(
                contentDescription = Res.string.tag_detail_memo_add_button_content_description,
                onClick = {},
            )
        }
    }
}
