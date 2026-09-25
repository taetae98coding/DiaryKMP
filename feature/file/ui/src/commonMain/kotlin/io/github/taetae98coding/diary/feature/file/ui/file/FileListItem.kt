package io.github.taetae98coding.diary.feature.file.ui.file

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_item_supporting_format
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileListItem(
    file: DiaryFile,
    modifier: Modifier = Modifier,
) {
    val createdAt = remember(file.createdAt) { file.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) }

    ListItem(
        headlineContent = {
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier.semantics(mergeDescendants = true) {},
        supportingContent = {
            Text(
                text =
                    stringResource(
                        Res.string.file_item_supporting_format,
                        file.size.toFileSize().toDisplayText(),
                        createdAt.date.toDisplayText(),
                        createdAt.time.toDisplayText(),
                    ),
            )
        },
        leadingContent = { FileIcon() },
    )
}

@ComponentPreview
@Composable
private fun FileListItemPreview() {
    DiaryTheme {
        FileListItem(
            file = previewDiaryFile(name = "보고서.pdf", size = 24_536_679),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
