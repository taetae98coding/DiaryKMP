package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.icon.FolderIcon
import io.github.taetae98coding.diary.compose.core.icon.UndoIcon
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItem
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewFileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_choose_directory
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_chosen_directory_supporting
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_default_location_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_download_location_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_download_location_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_reset_directory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingFileDownloadLocationSection(
    onChooseClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
    directory: FileDownloadDirectory? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Column {
            Text(
                text = stringResource(Res.string.setting_file_download_location_label),
                style = DiaryTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.setting_file_download_location_description),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.bodySmall,
            )
        }

        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
        ) {
            val count = if (directory == null) 1 else 2

            SettingFileCurrentLocationItem(
                onClick = onChooseClick,
                directory = directory,
                count = count,
            )
            if (directory != null) {
                DiarySegmentedListItem(
                    onClick = onResetClick,
                    index = 1,
                    count = count,
                    leadingContent = { UndoIcon() },
                ) {
                    Text(text = stringResource(Res.string.setting_file_reset_directory))
                }
            }
        }
    }
}

@Composable
private fun SettingFileCurrentLocationItem(
    onClick: () -> Unit,
    directory: FileDownloadDirectory?,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val chooseLabel = stringResource(Res.string.setting_file_choose_directory)

    DiarySegmentedListItem(
        onClick = onClick,
        modifier =
            modifier.semantics {
                onClick(label = chooseLabel) {
                    onClick()
                    true
                }
            },
        index = 0,
        count = count,
        leadingContent = { FolderIcon() },
        supportingContent = directory.supportingContent(),
    ) {
        Text(text = directory?.name?.ifEmpty { stringResource(Res.string.setting_file_chosen_directory_supporting) } ?: stringResource(Res.string.setting_file_default_location_label))
    }
}

private fun FileDownloadDirectory?.supportingContent(): (@Composable () -> Unit)? {
    if (this == null) return { Text(text = stringResource(settingFileDefaultLocationDescription)) }

    return when (val supportingText = supportingText()) {
        is SettingFileLocationSupportingText.Resource -> resourceSupportingContent(resource = supportingText.resource)
        is SettingFileLocationSupportingText.Text -> textSupportingContent(text = supportingText.text)
    }
}

private fun FileDownloadDirectory.resourceSupportingContent(resource: StringResource): (@Composable () -> Unit)? {
    if (name.isEmpty()) return null

    return { Text(text = stringResource(resource)) }
}

private fun textSupportingContent(text: String): @Composable () -> Unit = { Text(text = text) }

private class FileDownloadDirectoryPreviewParameter : PreviewParameterProvider<FileDownloadDirectory?> {
    override val values: Sequence<FileDownloadDirectory?> =
        sequenceOf(
            null,
            previewFileDownloadDirectory(),
        )
}

@ComponentPreview
@Composable
private fun SettingFileDownloadLocationSectionPreview(
    @PreviewParameter(FileDownloadDirectoryPreviewParameter::class) directory: FileDownloadDirectory?,
) {
    DiaryTheme {
        SettingFileDownloadLocationSection(
            onChooseClick = {},
            onResetClick = {},
            directory = directory,
        )
    }
}
