package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import kotlinx.datetime.LocalDateRange

@Composable
internal fun HolidayHomeScreen(
    navigateUp: () -> Unit,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    state: HolidayHomeScaffoldState,
    modifier: Modifier = Modifier,
) {
    val viewModelStoreProvider = rememberViewModelStoreProvider()

    HolidayHomeScaffold(
        onEvent = { event ->
            when (event) {
                is HolidayHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
            }
        },
        modifier = modifier,
        state = state,
        yearContent = { year ->
            HolidayHomeYearContent(
                year = year,
                state = state,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoAdd = navigateToMemoAdd,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}
