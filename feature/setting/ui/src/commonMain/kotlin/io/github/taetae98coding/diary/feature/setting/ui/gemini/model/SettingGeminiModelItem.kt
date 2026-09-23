package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

@Composable
internal fun SettingGeminiModelItem(
    model: GeminiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(vertical = SettingGeminiModelDialogDefaults.ItemVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = model.displayName.ifEmpty { model.id },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = model.id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (model.description.isNotEmpty()) {
                Text(
                    text = model.description,
                    maxLines = SettingGeminiModelDialogDefaults.ITEM_DESCRIPTION_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun SettingGeminiModelItemPreview() {
    DiaryTheme {
        Surface {
            SettingGeminiModelItem(
                model =
                    GeminiModel(
                        id = "models/gemini-3.6-flash",
                        displayName = "Gemini 3.6 Flash",
                        description = "빠르고 저렴한 범용 모델입니다.",
                    ),
                onClick = {},
                selected = true,
            )
        }
    }
}
