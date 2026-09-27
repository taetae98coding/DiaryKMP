package io.github.taetae98coding.diary.feature.file.ui.file

import androidx.compose.foundation.layout.Row
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
import io.github.taetae98coding.diary.feature.file.ui.file_item_supporting_detail_format
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
                text = file.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier.semantics(mergeDescendants = true) {},
        supportingContent = {
            FileListItemSupportingText(
                name = file.name,
                detail =
                    stringResource(
                        Res.string.file_item_supporting_detail_format,
                        file.size.toFileSize().toDisplayText(),
                        createdAt.date.toDisplayText(),
                        createdAt.time.toDisplayText(),
                    ),
            )
        },
        leadingContent = { FileIcon() },
    )
}

@Composable
private fun FileListItemSupportingText(
    name: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(
            text = name,
            modifier = Modifier.weight(weight = 1F, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = detail,
            maxLines = 1,
        )
    }
}

@ComponentPreview
@Composable
private fun FileListItemPreview() {
    DiaryTheme {
        FileListItem(
            file = previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
