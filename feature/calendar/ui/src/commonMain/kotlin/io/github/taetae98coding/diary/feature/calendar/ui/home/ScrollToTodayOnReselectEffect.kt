package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ScrollToTodayOnReselectEffect(
    reselectEvent: Flow<Unit> = emptyFlow(),
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    CollectEffect(effect = reselectEvent) {
        state.animateScrollToToday()
    }
}
