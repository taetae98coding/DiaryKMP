package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class RoutineViewScopeListTest :
    FunSpec({
        test("오늘 할 루틴, 모든 루틴 순서로 둔다") {
            routineViewScopeList shouldContainExactly
                listOf(
                    RoutineViewScope.TODAY,
                    RoutineViewScope.ALL,
                )
        }

        test("모든 보기 범위를 한 번씩만 담는다") {
            routineViewScopeList shouldContainExactlyInAnyOrder RoutineViewScope.entries
        }
    })
