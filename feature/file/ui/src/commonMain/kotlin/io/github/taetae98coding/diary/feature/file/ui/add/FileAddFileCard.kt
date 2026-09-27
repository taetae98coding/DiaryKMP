package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file.toDisplayText
import io.github.taetae98coding.diary.feature.file.ui.file.toFileSize
import io.github.taetae98coding.diary.feature.file.ui.file_add_no_file_selected
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileAddFileCard(
    onChooseClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFileProvider: () -> FileUploadSource? = { null },
) {
    OutlinedCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FileAddFileCardItem(
                modifier = Modifier.weight(1F),
                selectedFileProvider = selectedFileProvider,
            )
            FileAddChooseFileButton(
                onClick = onChooseClick,
                modifier = Modifier.padding(end = DiaryTheme.dimens.itemSpacing),
            )
        }
    }
}

@Composable
private fun FileAddFileCardItem(
    modifier: Modifier = Modifier,
    selectedFileProvider: () -> FileUploadSource? = { null },
) {
    val selectedFile = selectedFileProvider()

    ListItem(
        headlineContent = {
            if (selectedFile == null) {
                Text(
                    text = stringResource(Res.string.file_add_no_file_selected),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = selectedFile.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier =
            modifier
                .heightIn(min = FileAddFileCardDefaults.MinHeight)
                .semantics(mergeDescendants = true) {},
        supportingContent =
            selectedFile?.let { file ->
                { Text(text = file.size.toFileSize().toDisplayText()) }
            },
        leadingContent = { FileIcon() },
    )
}

private class SelectedFilePreviewParameter : PreviewParameterProvider<FileUploadSource?> {
    override val values: Sequence<FileUploadSource?> =
        sequenceOf(
            null,
            FileUploadSource(uri = FileUri(""), name = "보고서.pdf", mimeType = "application/pdf", size = 24_536_679),
        )
}

@ComponentPreview
@Composable
private fun FileAddFileCardPreview(
    @PreviewParameter(SelectedFilePreviewParameter::class) selectedFile: FileUploadSource?,
) {
    DiaryTheme {
        FileAddFileCard(
            onChooseClick = {},
            modifier = Modifier.fillMaxWidth(),
            selectedFileProvider = { selectedFile },
        )
    }
}
