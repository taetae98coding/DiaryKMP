package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.FolderOpenIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_add_choose_file
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileAddChooseFileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.file_add_choose_file)

    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            FolderOpenIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun FileAddChooseFileButtonPreview() {
    DiaryTheme {
        Surface {
            FileAddChooseFileButton(onClick = {})
        }
    }
}
