package io.github.taetae98coding.diary.feature.routine.ui.form

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class RoutineCycleListTest :
    FunSpec({
        test("매일, 평일만, 주말만, 매주, 매월 순서로 둔다") {
            routineCycleList shouldContainExactly
                listOf(
                    RoutineCycle.DAILY,
                    RoutineCycle.WEEKDAYS,
                    RoutineCycle.WEEKENDS,
                    RoutineCycle.WEEKLY,
                    RoutineCycle.MONTHLY,
                )
        }

        test("모든 주기를 한 번씩만 담는다") {
            routineCycleList shouldContainExactlyInAnyOrder RoutineCycle.entries
        }
    })
