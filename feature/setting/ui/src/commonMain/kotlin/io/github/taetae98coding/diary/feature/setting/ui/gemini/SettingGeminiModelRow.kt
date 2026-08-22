package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.icon.NextIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewGeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_missing_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_unselected
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiModelRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
    isMissingFromLoadedListProvider: () -> Boolean = { false },
) {
    val model = state.model
    val isMissingFromLoadedList = isMissingFromLoadedListProvider()
    val label = stringResource(Res.string.setting_gemini_model_label)
    val value = model.ifEmpty { stringResource(Res.string.setting_gemini_model_unselected) }
    val missingMessage = stringResource(Res.string.setting_gemini_model_missing_message)

    Card(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp)
                    .clearAndSetSemantics {
                        contentDescription = if (isMissingFromLoadedList) "$label, $value, $missingMessage" else "$label, $value"
                    },
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (isMissingFromLoadedList) {
                    Text(
                        text = missingMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            NextIcon()
        }
    }
}

@ComponentPreview
@Composable
private fun SettingGeminiModelRowPreview() {
    DiaryTheme {
        Surface {
            SettingGeminiModelRow(
                onClick = {},
                state = rememberSettingGeminiFormState(initialSetting = previewGeminiSetting()),
                isMissingFromLoadedListProvider = { true },
            )
        }
    }
}
