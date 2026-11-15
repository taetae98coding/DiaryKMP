package io.github.taetae98coding.diary.feature.routine.ui.home.viewmode

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class RoutineHomeViewModeListTest :
    FunSpec({
        test("목록 보기, 달력 보기 순서로 둔다") {
            routineHomeViewModeList shouldContainExactly
                listOf(
                    RoutineHomeViewMode.LIST,
                    RoutineHomeViewMode.CALENDAR,
                )
        }

        test("모든 보기를 한 번씩만 담는다") {
            routineHomeViewModeList shouldContainExactlyInAnyOrder RoutineHomeViewMode.entries
        }
    })
