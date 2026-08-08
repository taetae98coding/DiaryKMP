package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_home_year_title
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HolidayHomeTopBar(
    onEvent: (HolidayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(Res.string.holiday_home_year_title, state.year.toString())) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(HolidayHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.holiday_navigate_up_button_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun HolidayHomeTopBarPreview() {
    DiaryTheme {
        HolidayHomeTopBar(onEvent = {})
    }
}
