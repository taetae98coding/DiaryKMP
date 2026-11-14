package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.runtime.saveable.SaverScope
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate

class RoutineRuleInputStateTest : FunSpec() {
    init {
        test("시작 날짜를 고르면 고른 날짜가 시작 날짜가 되고 주기는 그대로다") {
            val selected = Arb.kotlinxLocalDate().next()
            val state = state(cycle = RoutineCycle.WEEKLY)

            state.selectStartDate(date = selected)

            state.startDate shouldBe selected
            state.cycle shouldBe RoutineCycle.WEEKLY
        }

        test("TC-aedbfaff-6672-4617-9c1d-3533fa4b5a1d 주기를 바꾸면 주기만 바뀐다") {
            val startDate = Arb.kotlinxLocalDate().next()
            val state = state(startDate = startDate, cycle = RoutineCycle.DAILY)

            listOf(RoutineCycle.WEEKDAYS, RoutineCycle.WEEKENDS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY, RoutineCycle.DAILY).forEach { cycle ->
                state.selectCycle(cycle = cycle)

                state.cycle shouldBe cycle
                state.startDate shouldBe startDate
            }
        }

        test("TC-81afe219-36ea-43b0-89c5-e04122c6fdcd 처음 상태로 되돌리면 넘긴 시작 날짜와 처음 값이 된다") {
            val startDate = Arb.kotlinxLocalDate().next()
            val state = state(cycle = RoutineCycle.WEEKLY)

            state.reset(startDate = startDate)

            state.startDate shouldBe startDate
            state.cycle shouldBe ROUTINE_DEFAULT_CYCLE
        }

        test("저장했다가 복원하면 두 값을 그대로 돌려준다") {
            val state =
                state(
                    startDate = Arb.kotlinxLocalDate().next(),
                    cycle = Arb.enum<RoutineCycle>().next(),
                )

            val saved = with(RoutineRuleInputState.Saver) { SaverScope { true }.save(state) }.shouldNotBeNull()
            val restored = RoutineRuleInputState.Saver.restore(saved).shouldNotBeNull()

            restored.startDate shouldBe state.startDate
            restored.cycle shouldBe state.cycle
        }
    }

    private companion object {
        fun state(
            startDate: LocalDate = Arb.kotlinxLocalDate().next(),
            cycle: RoutineCycle = ROUTINE_DEFAULT_CYCLE,
        ): RoutineRuleInputState =
            RoutineRuleInputState(
                startDate = startDate,
                cycle = cycle,
            )
    }
}
