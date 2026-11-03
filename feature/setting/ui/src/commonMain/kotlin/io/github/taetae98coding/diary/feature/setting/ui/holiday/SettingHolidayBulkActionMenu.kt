package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenu
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuIcon
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuItem
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
    DiaryFloatingActionButtonMenu(
        expanded = state.isBulkActionExpanded,
        onExpandedChange = { isExpanded -> if (isExpanded) state.expandBulkAction() else state.collapseBulkAction() },
        icon = DiaryFloatingActionButtonMenuIcon.CHECKLIST,
        contentDescription = stringResource(Res.string.setting_holiday_bulk_action_content_description),
        expandedContentDescription = stringResource(Res.string.setting_holiday_bulk_action_close_content_description),
        items =
            listOf(
                DiaryFloatingActionButtonMenuItem(
                    text = stringResource(Res.string.setting_holiday_select_all),
                    icon = DiaryFloatingActionButtonMenuIcon.SELECT_ALL,
                    onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickSelectAll) },
                ),
                DiaryFloatingActionButtonMenuItem(
                    text = stringResource(Res.string.setting_holiday_deselect_all),
                    icon = DiaryFloatingActionButtonMenuIcon.DESELECT,
                    onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickDeselectAll) },
                ),
                DiaryFloatingActionButtonMenuItem(
                    text = stringResource(Res.string.setting_holiday_select_days_off),
                    icon = DiaryFloatingActionButtonMenuIcon.DAY_OFF,
                    onClick = { selectBulkAction(state = state, onEvent = onEvent, event = SettingHolidayScaffoldEvent.ClickSelectDaysOff) },
                ),
            ),
        modifier = modifier,
    )
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
