package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import kotlinx.datetime.LocalDate

@Stable
internal class RoutineRuleInputState(
    startDate: LocalDate,
    cycle: RoutineCycle,
) {
    var startDate: LocalDate by mutableStateOf(startDate)
        private set

    var cycle: RoutineCycle by mutableStateOf(cycle)
        private set

    fun selectStartDate(date: LocalDate) {
        startDate = date
    }

    fun selectCycle(cycle: RoutineCycle) {
        this.cycle = cycle
    }

    fun reset(startDate: LocalDate) {
        this.startDate = startDate
        cycle = ROUTINE_DEFAULT_CYCLE
    }

    companion object {
        private const val START_DATE_KEY = "startDate"
        private const val CYCLE_KEY = "cycle"

        val Saver: Saver<RoutineRuleInputState, Any> =
            mapSaver(
                save = { state ->
                    mapOf(
                        START_DATE_KEY to state.startDate.toEpochDays(),
                        CYCLE_KEY to state.cycle.name,
                    )
                },
                restore = { saved ->
                    RoutineRuleInputState(
                        startDate = LocalDate.fromEpochDays(saved[START_DATE_KEY] as Long),
                        cycle = RoutineCycle.valueOf(saved[CYCLE_KEY] as String),
                    )
                },
            )
    }
}

@Composable
internal fun rememberRoutineRuleInputState(
    initialStartDate: LocalDate,
    initialCycle: RoutineCycle = ROUTINE_DEFAULT_CYCLE,
): RoutineRuleInputState =
    rememberSaveable(saver = RoutineRuleInputState.Saver) {
        RoutineRuleInputState(
            startDate = initialStartDate,
            cycle = initialCycle,
        )
    }
