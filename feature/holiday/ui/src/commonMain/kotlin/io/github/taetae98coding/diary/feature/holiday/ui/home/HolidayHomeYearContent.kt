package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
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
        )
    }
}

@Composable
private fun FetchHolidayEffect(viewModel: HolidayHomeYearViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.fetch()
    }
}

@Composable
private fun UpdateAnnualLeaveCountEffect(
    viewModel: HolidayHomeYearViewModel,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
) {
    LaunchedEffect(state, viewModel) {
        snapshotFlow { state.annualLeaveCount }
            .collect { annualLeaveCount -> viewModel.updateAnnualLeaveCount(annualLeaveCount = annualLeaveCount) }
    }
}
