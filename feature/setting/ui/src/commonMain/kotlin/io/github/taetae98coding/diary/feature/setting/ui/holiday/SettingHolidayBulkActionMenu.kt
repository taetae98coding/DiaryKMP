@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.icon.ClearIcon
import io.github.taetae98coding.diary.compose.core.icon.DayOffIcon
import io.github.taetae98coding.diary.compose.core.icon.DeselectIcon
import io.github.taetae98coding.diary.compose.core.icon.SelectAllIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_bulk_action_close_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_bulk_action_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_deselect_all
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_select_all
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_select_days_off
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidayBulkActionMenu(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
) {
    FloatingActionButtonMenu(
        expanded = state.isBulkActionExpanded,
        button = {
            ToggleFloatingActionButton(
                checked = state.isBulkActionExpanded,
                onCheckedChange = { isChecked -> if (isChecked) state.expandBulkAction() else state.collapseBulkAction() },
            ) {
                val isClosing by remember { derivedStateOf { checkedProgress > SettingHolidayBulkActionMenuDefaults.CLOSE_ICON_PROGRESS } }
                val iconModifier =
                    with(ToggleFloatingActionButtonDefaults) {
                        Modifier.animateIcon(checkedProgress = { checkedProgress })
                    }

                if (isClosing) {
                    ClearIcon(
                        modifier = iconModifier,
                        contentDescription = stringResource(Res.string.setting_holiday_bulk_action_close_content_description),
                    )
                } else {
                    ChecklistIcon(
                        modifier = iconModifier,
                        contentDescription = stringResource(Res.string.setting_holiday_bulk_action_content_description),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        FloatingActionButtonMenuItem(
            onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickSelectAll) },
            text = { Text(text = stringResource(Res.string.setting_holiday_select_all)) },
            icon = { SelectAllIcon() },
        )
        FloatingActionButtonMenuItem(
            onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickDeselectAll) },
            text = { Text(text = stringResource(Res.string.setting_holiday_deselect_all)) },
            icon = { DeselectIcon() },
        )
        FloatingActionButtonMenuItem(
            onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickSelectDaysOff) },
            text = { Text(text = stringResource(Res.string.setting_holiday_select_days_off)) },
            icon = { DayOffIcon() },
        )
    }
}

private fun selectBulkAction(
    state: SettingHolidayScaffoldState,
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    event: SettingHolidayScaffoldEvent,
) {
    state.collapseBulkAction()
    onEvent(event)
}

@ComponentPreview
@Composable
private fun SettingHolidayBulkActionMenuPreview() {
    DiaryTheme {
        Surface {
            SettingHolidayBulkActionMenu(onEvent = {})
        }
    }
}
