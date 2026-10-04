package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
internal fun FetchHolidayEffect(viewModel: HolidayHomeYearViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.fetch()
    }
}

@Composable
internal fun UpdateAnnualLeaveCountEffect(
    viewModel: HolidayHomeYearViewModel,
    state: HolidayHomeScaffoldState,
) {
    LaunchedEffect(state, viewModel) {
        snapshotFlow { state.annualLeaveCount }
            .collect { annualLeaveCount -> viewModel.updateAnnualLeaveCount(annualLeaveCount = annualLeaveCount) }
    }
}
