package io.github.taetae98coding.diary.compose.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
public class RoutineListState(
    initialToday: LocalDate,
    private val clock: Clock = Clock.System,
) {
    public var today: LocalDate by mutableStateOf(initialToday)
        private set

    public fun updateToday() {
        today = clock.todayIn(TimeZone.currentSystemDefault())
    }
}

@Composable
public fun rememberRoutineListState(initialToday: LocalDate? = null): RoutineListState =
    remember {
        RoutineListState(initialToday = initialToday ?: Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
