package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.key
import kotlinx.datetime.LocalDateRange
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun HolidayHomeYearContent(
    year: Int,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = year, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val viewModel = koinViewModel<HolidayHomeYearViewModel> { parametersOf(year) }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        FetchHolidayEffect(viewModel = viewModel)
        UpdateAnnualLeaveCountEffect(
            state = state,
            viewModel = viewModel,
        )
        GoldenHolidayYear(
            uiStateProvider = { uiState },
            onEvent = { event ->
                when (event) {
                    is HolidayHomeYearContentEvent.ClickRetry -> viewModel.fetch()
                    is HolidayHomeYearContentEvent.SelectDate -> navigateToMemoAdd(event.dateRange)
                }
            },
            modifier = modifier,
            contentPadding = contentPadding,
        )
    }
}
