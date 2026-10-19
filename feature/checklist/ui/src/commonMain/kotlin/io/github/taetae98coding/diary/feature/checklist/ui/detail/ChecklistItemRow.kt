package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_item_remove_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemRemoveButton
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklistItem
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistItemRow(
    onClick: () -> Unit,
    onRemove: () -> Unit,
    item: ChecklistItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(end = DiaryTheme.dimens.screenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1F)
                    .heightIn(min = ChecklistItemRowDefaults.MinHeight)
                    .toggleable(
                        value = item.isChecked,
                        role = Role.Checkbox,
                        onValueChange = { onClick() },
                    ).padding(start = DiaryTheme.dimens.screenHorizontalPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = null,
            )
            Text(
                text = item.content,
                modifier =
                    Modifier
                        .weight(1F)
                        .padding(horizontal = DiaryTheme.dimens.itemSpacing),
                color = if (item.isChecked) DiaryTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                style = DiaryTheme.typography.bodyLarge,
            )
        }
        ChecklistItemRemoveButton(
            onClick = onRemove,
            contentDescription = stringResource(Res.string.checklist_detail_item_remove_button_content_description, item.content),
        )
    }
}

@ComponentPreview
@Composable
private fun ChecklistItemRowPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isChecked: Boolean,
) {
    DiaryTheme {
        Surface {
            ChecklistItemRow(
                onClick = {},
                onRemove = {},
                item = previewChecklistItem(content = "여권 챙기기", isChecked = isChecked),
            )
        }
    }
}
