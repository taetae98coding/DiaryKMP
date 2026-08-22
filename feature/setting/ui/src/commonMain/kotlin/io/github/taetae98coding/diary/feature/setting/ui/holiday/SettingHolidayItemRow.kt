package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_day_off_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidayItemRow(
    holidaySetting: HolidaySetting,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = holidaySetting.isVisible,
                        role = Role.Checkbox,
                        onValueChange = { onClick() },
                    ).semantics(mergeDescendants = true) {}
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = holidaySetting.isVisible,
                onCheckedChange = null,
            )
            Text(
                text = holidaySetting.name,
                modifier = Modifier.weight(1F),
                style = DiaryTheme.typography.bodyLarge,
            )
            if (holidaySetting.isHoliday) {
                DayOffBadge()
            }
        }
    }
}

@Composable
private fun DayOffBadge(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.CircleShape,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = DiaryTheme.colorScheme.errorContainer,
        contentColor = DiaryTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = stringResource(Res.string.setting_holiday_day_off_label),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = DiaryTheme.typography.labelMedium,
        )
    }
}

@ComponentPreview
@Composable
private fun SettingHolidayItemRowPreview() {
    DiaryTheme {
        SettingHolidayItemRow(
            holidaySetting =
                HolidaySetting(
                    key = "대체공휴일",
                    name = "대체 공휴일",
                    isHoliday = true,
                    isVisible = true,
                ),
            onClick = {},
        )
    }
}
