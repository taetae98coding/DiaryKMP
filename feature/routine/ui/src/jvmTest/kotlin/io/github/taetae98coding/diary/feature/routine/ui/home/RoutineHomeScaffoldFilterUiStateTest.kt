package io.github.taetae98coding.diary.feature.routine.ui.home

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class RoutineHomeScaffoldFilterUiStateTest :
    FunSpec({
        test("TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 빈 상태에서 알리는 내용은 태그 필터의 적용 여부와 완료되지 않은 루틴이 있는지로 가른다") {
            val tagA = Arb.kotlinUuid().next()

            // 선택한 태그가 모두 선택할 수 없게 되면 적용 중인 선택은 비고 저장된 선택만 남는다.
            listOf(
                state(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false) to RoutineHomeEmptyReason.NO_ROUTINE,
                state(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = false) to RoutineHomeEmptyReason.NO_ROUTINE,
                state(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to RoutineHomeEmptyReason.NO_TODAY_ROUTINE,
                state(storedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to RoutineHomeEmptyReason.NO_TODAY_ROUTINE,
                state(selectedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to RoutineHomeEmptyReason.NO_FILTERED_ROUTINE,
                state(selectedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true) to RoutineHomeEmptyReason.NO_FILTERED_ROUTINE,
                state(selectedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false) to RoutineHomeEmptyReason.NO_FILTERED_ROUTINE,
            ).forEach { (state, expected) ->
                (state to state.emptyReason) shouldBe (state to expected)
            }
        }

        test("TC-24ebfe5d-b3a9-48c4-9f6e-c1d6b3d152d9 완료되지 않은 루틴은 있지만 오늘 할 루틴이 없으면 오늘 할 루틴이 없음을 알린다") {
            state(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true).emptyReason shouldBe RoutineHomeEmptyReason.NO_TODAY_ROUTINE
        }

        test("TC-68d686f2-29c1-4d2d-aefe-55fd3e84df8f 완료되지 않은 루틴의 유무가 달라지면 빈 상태에서 알리는 내용도 바뀐다") {
            val empty = state(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false)

            empty.emptyReason shouldBe RoutineHomeEmptyReason.NO_ROUTINE
            empty.copy(hasUnfinishedRoutine = true).emptyReason shouldBe RoutineHomeEmptyReason.NO_TODAY_ROUTINE
            empty.copy(hasUnfinishedRoutine = true).copy(hasUnfinishedRoutine = false).emptyReason shouldBe RoutineHomeEmptyReason.NO_ROUTINE
        }

        test("보기 범위가 모든 루틴이면 완료되지 않은 루틴이 있어도 오늘 할 루틴이 없다고 알리지 않는다") {
            // 모든 루틴에서 목록이 비는 것은 조회가 아직 따라오지 않은 사이뿐이므로 루틴이 없을 때의 안내로 둔다.
            state(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true).emptyReason shouldBe RoutineHomeEmptyReason.NO_ROUTINE
        }

        test("TC-7d9ad72e-b2d5-4ab4-9a3f-3fc34c3b23cb 보기 범위는 필터 적용 여부에 영향을 주지 않는다") {
            val tagA = Arb.kotlinUuid().next()

            listOf(
                state(viewScope = RoutineViewScope.TODAY) to false,
                state(viewScope = RoutineViewScope.ALL) to false,
                state(selectedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.TODAY) to true,
                state(selectedTagIdSet = setOf(tagA), viewScope = RoutineViewScope.ALL) to true,
            ).forEach { (state, expected) ->
                state.isApplied shouldBe expected
            }
        }

        test("TC-7611b734-5a81-486f-b5d5-b5c1e2004421 보기 범위를 다른 값으로 바꾸면 목록을 처음부터 다시 보게 하는 기준이 달라진다") {
            val today = state(viewScope = RoutineViewScope.TODAY)
            val all = state(viewScope = RoutineViewScope.ALL)

            today.listQueryFilter shouldNotBe all.listQueryFilter
            all.listQueryFilter shouldNotBe today.listQueryFilter
        }

        test("TC-8389da95-7521-4222-9754-2e7007fb3d13 보기 범위가 그대로면 목록을 처음부터 다시 보게 하는 기준도 그대로다") {
            RoutineViewScope.entries.forEach { viewScope ->
                val before = state(viewScope = viewScope, hasUnfinishedRoutine = false)
                val after = state(viewScope = viewScope, hasUnfinishedRoutine = true)

                after.listQueryFilter shouldBe before.listQueryFilter
            }
        }

        test("저장된 필터를 아직 불러오지 못했으면 목록을 처음부터 다시 보게 하는 기준이 없다") {
            RoutineHomeScaffoldFilterUiState(isLoaded = false).listQueryFilter.shouldBeNull()
        }

        test("선택할 수 없게 된 태그가 빠져 적용 중인 선택만 달라져도 목록을 처음부터 다시 보게 하는 기준은 그대로다") {
            val tagA = Arb.kotlinUuid().next()

            state(selectedTagIdSet = setOf(tagA), storedTagIdSet = setOf(tagA)).listQueryFilter shouldBe state(storedTagIdSet = setOf(tagA)).listQueryFilter
        }
    })

private fun state(
    selectedTagIdSet: Set<Uuid> = emptySet(),
    storedTagIdSet: Set<Uuid> = selectedTagIdSet,
    viewScope: RoutineViewScope = RoutineViewScope.TODAY,
    hasUnfinishedRoutine: Boolean = false,
): RoutineHomeScaffoldFilterUiState =
    RoutineHomeScaffoldFilterUiState(
        selectedTagIdSet = selectedTagIdSet,
        storedTagIdSet = storedTagIdSet,
        viewScope = viewScope,
        hasUnfinishedRoutine = hasUnfinishedRoutine,
    )
