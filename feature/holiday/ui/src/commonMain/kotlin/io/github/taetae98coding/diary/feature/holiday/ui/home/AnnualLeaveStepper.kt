package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.icon.RemoveIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_annual_leave_decrease_content_description
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_annual_leave_increase_content_description
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_annual_leave_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AnnualLeaveStepper(
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.holiday_annual_leave_label),
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.titleSmall,
        )
        OutlinedIconButton(
            onClick = state::decreaseAnnualLeave,
            enabled = state.canDecreaseAnnualLeave,
        ) {
            RemoveIcon(contentDescription = stringResource(Res.string.holiday_annual_leave_decrease_content_description))
        }
        Text(
            text = state.annualLeaveCount.toString(),
            style = DiaryTheme.typography.titleMedium,
        )
        OutlinedIconButton(onClick = state::increaseAnnualLeave) {
            AddIcon(contentDescription = stringResource(Res.string.holiday_annual_leave_increase_content_description))
        }
    }
}

@ComponentPreview
@Composable
private fun AnnualLeaveStepperPreview() {
    DiaryTheme {
        Surface {
            AnnualLeaveStepper()
        }
    }
}
