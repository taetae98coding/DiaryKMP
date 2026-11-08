package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.github.taetae98coding.diary.domain.dday.preset.value
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_preset_baby
import io.github.taetae98coding.diary.feature.dday.ui.dday_preset_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_preset_couple
import io.github.taetae98coding.diary.feature.dday.ui.dday_preset_custom
import io.github.taetae98coding.diary.feature.dday.ui.dday_preset_exam
import org.jetbrains.compose.resources.stringResource

internal val ddayPresetList: List<DDayPreset> = listOf(DDayPreset.COUPLE, DDayPreset.EXAM, DDayPreset.BABY, DDayPreset.CUSTOM)

@Composable
internal fun DDayPresetRow(
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
) {
    val presetContentDescription = stringResource(Res.string.dday_preset_content_description)

    Row(
        modifier =
            modifier
                .semantics { contentDescription = presetContentDescription }
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ddayPresetList.forEach { preset ->
            DiaryAssistChip(
                label = preset.label(),
                onClick = { state.ruleState.applyPreset(value = preset.value()) },
            )
        }
    }
}

@Composable
internal fun DDayPreset.label(): String =
    when (this) {
        DDayPreset.COUPLE -> stringResource(Res.string.dday_preset_couple)
        DDayPreset.EXAM -> stringResource(Res.string.dday_preset_exam)
        DDayPreset.BABY -> stringResource(Res.string.dday_preset_baby)
        DDayPreset.CUSTOM -> stringResource(Res.string.dday_preset_custom)
    }

@ComponentPreview
@Composable
private fun DDayPresetRowPreview() {
    DiaryTheme {
        Surface {
            DDayPresetRow()
        }
    }
}
