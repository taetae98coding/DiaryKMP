package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
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
    val title = stringResource(Res.string.holiday_home_year_title, state.year.toString())
    val navigateUpContentDescription = stringResource(Res.string.holiday_navigate_up_button_content_description)

    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        isTitleCentered = true,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(HolidayHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
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
