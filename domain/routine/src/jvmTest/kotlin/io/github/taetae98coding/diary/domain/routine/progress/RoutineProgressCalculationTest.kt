package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate

class RoutineProgressCalculationTest :
    FunSpec({
        test("TC-a624a684-0e13-4102-995b-7318bfa2cf95 오늘과 시작 날짜로 진행 상태를 정한다") {
            val detail = Arb.routineDetail(startDate = LocalDate(2026, 11, 10)).next()

            listOf(
                LocalDate(2026, 11, 9) to RoutineProgress.NOT_STARTED,
                LocalDate(2026, 11, 10) to RoutineProgress.IN_PROGRESS,
                LocalDate(2026, 11, 11) to RoutineProgress.IN_PROGRESS,
                LocalDate(2099, 12, 31) to RoutineProgress.IN_PROGRESS,
            ).forEach { (today, expected) ->
                detail.progress(today = today) shouldBe expected
            }
        }

        test("TC-f623479e-76d0-4e37-b5d2-f683348a86a1 기록의 유무와 목표를 채웠는지는 진행 상태에 영향을 주지 않는다") {
            // 진행 상태는 기록을 입력으로 받지 않으므로, 기록이 없을 때와 목표를 채운 기록이 있을 때의 판정은 같은 판정이다.
            val detail =
                Arb
                    .routineDetail(
                        startDate = LocalDate(2026, 11, 10),
                        cycle = RoutineCycle.MONTHLY,
                        goalAmount = 3,
                    ).next()

            detail.progress(today = LocalDate(2026, 11, 13)) shouldBe RoutineProgress.IN_PROGRESS
            detail.progress(today = LocalDate(2026, 11, 9)) shouldBe RoutineProgress.NOT_STARTED
        }

        test("TC-be2d88be-ae77-4c97-a3d7-83e01a7ce559 완료 여부를 바꿔도 진행 상태는 그대로다") {
            val today = LocalDate(2026, 11, 13)

            listOf(
                LocalDate(2026, 11, 10) to RoutineProgress.IN_PROGRESS,
                LocalDate(2026, 11, 13) to RoutineProgress.IN_PROGRESS,
                LocalDate(2026, 11, 14) to RoutineProgress.NOT_STARTED,
            ).forEach { (startDate, expected) ->
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail(startDate = startDate).next()).next()
                val finished = routine.copy(isFinished = true)
                val restarted = finished.copy(isFinished = false)

                listOf(routine, finished, restarted).forEach { state ->
                    state.detail.progress(today = today) shouldBe expected
                }
            }
        }

        test("TC-8ef53f11-7827-4300-b067-7e68f631b156 달성률은 백분율의 소수점 아래를 버리고 100을 넘지 않는다") {
            listOf(
                Triple(2000, 0L, 0),
                Triple(2000, 1500L, 75),
                Triple(2000, 1999L, 99),
                Triple(2000, 2000L, 100),
                Triple(2000, 2500L, 100),
                Triple(3, 1L, 33),
                Triple(3, 2L, 66),
                Triple(3, 3L, 100),
                Triple(1, 1L, 100),
                Triple(9_999_999, 1L, 0),
                Triple(9_999_999, 9_999_998L, 99),
            ).forEach { (goalAmount, cycleAmount, expected) ->
                val detail = Arb.routineDetail(startDate = LocalDate(2026, 1, 1), goalAmount = goalAmount).next()

                detail.achievementRate(cycleAmount = cycleAmount) shouldBe expected
            }
        }

        test("TC-21d3bedb-3a13-4eef-9240-fcbebb2c3a75 목표량이 없는 루틴은 달성률을 갖지 않는다") {
            val detail = Arb.routineDetail(startDate = LocalDate(2026, 1, 1), goalAmount = null).next()

            listOf(0L, 1500L).forEach { cycleAmount ->
                detail.achievementRate(cycleAmount = cycleAmount).shouldBeNull()
            }
        }

        test("TC-4210b8a5-ba71-4a36-b7dc-672982539931 달성률이 100이어도 합계가 늘어난 뒤의 달성률은 100이다") {
            val detail = Arb.routineDetail(startDate = LocalDate(2026, 1, 1), goalAmount = 3).next()

            detail.achievementRate(cycleAmount = 3L) shouldBe 100
            detail.canRecord(date = LocalDate(2026, 11, 13)) shouldBe true
            detail.achievementRate(cycleAmount = 4L) shouldBe 100
        }

        test("합계가 Int 범위를 넘어도 달성률은 100에서 멈춘다") {
            val detail = Arb.routineDetail(startDate = LocalDate(2026, 1, 1), goalAmount = 1).next()

            detail.achievementRate(cycleAmount = Long.MAX_VALUE / 100) shouldBe 100
        }

        test("TC-5a8c94b7-944a-4098-bd91-5abc97871ece 완료되지 않았고 진행 중이며 목표량이 있는 루틴은 합계와 달성률을 낸다") {
            val today = LocalDate(2026, 11, 13)

            listOf(LocalDate(2026, 11, 1), today).forEach { startDate ->
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail(startDate = startDate, goalAmount = 2000).next()).next()
                val summary = RoutineSummary(routine = routine, cycleAmount = 1500L)

                summary.hasCycleProgress(today = today) shouldBe true
                routine.detail.achievementRate(cycleAmount = summary.cycleAmount) shouldBe 75
            }
        }

        test("TC-982f0c46-607d-4650-a6a5-d7df1024100f 완료되지 않았고 진행 중이며 목표량이 없는 루틴은 합계만 낸다") {
            val today = LocalDate(2026, 11, 13)

            listOf(LocalDate(2026, 11, 1), today).forEach { startDate ->
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail(startDate = startDate, goalAmount = null).next()).next()
                val summary = RoutineSummary(routine = routine, cycleAmount = Arb.long(0L..9_999_999L).next())

                summary.hasCycleProgress(today = today) shouldBe true
                routine.detail.achievementRate(cycleAmount = summary.cycleAmount).shouldBeNull()
            }
        }

        test("TC-2f9e855f-abab-4459-8673-5d664684d64e 완료되지 않았고 시작 전인 루틴은 합계와 달성률을 내지 않는다") {
            val today = LocalDate(2026, 11, 13)

            listOf(null to 0L, 2000 to 0L, 2000 to 500L, null to 500L).forEach { (goalAmount, cycleAmount) ->
                val detail = Arb.routineDetail(startDate = LocalDate(2026, 11, 20), cycle = RoutineCycle.MONTHLY, goalAmount = goalAmount).next()
                val summary = RoutineSummary(routine = Arb.routine(isDeleted = false, detail = detail).next(), cycleAmount = cycleAmount)

                summary.hasCycleProgress(today = today) shouldBe false
            }
        }

        test("TC-becafe28-4267-4e23-9396-08b9fca0cd41 완료된 루틴은 진행 상태와 관계없이 합계와 달성률을 내지 않는다") {
            val today = LocalDate(2026, 11, 13)

            listOf(LocalDate(2026, 11, 1), today, LocalDate(2026, 11, 20)).forEach { startDate ->
                listOf(null to 0L, 2000 to 1500L, 2000 to 2500L, null to 1500L).forEach { (goalAmount, cycleAmount) ->
                    val detail = Arb.routineDetail(startDate = startDate, cycle = RoutineCycle.MONTHLY, goalAmount = goalAmount).next()
                    val summary = RoutineSummary(routine = Arb.routine(isDeleted = false, detail = detail, isFinished = true).next(), cycleAmount = cycleAmount)

                    summary.hasCycleProgress(today = today) shouldBe false
                }
            }
        }

        test("TC-2652e6ab-e46c-4e3b-ab40-6b320278a02c 완료하면 합계와 달성률을 내지 않고 다시 시작하면 완료 전으로 돌아간다") {
            val today = LocalDate(2026, 11, 13)

            listOf(LocalDate(2026, 11, 1) to true, LocalDate(2026, 11, 20) to false).forEach { (startDate, expected) ->
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail(startDate = startDate).next()).next()
                val summary = RoutineSummary(routine = routine, cycleAmount = Arb.long(0L..9_999_999L).next())
                val finished = summary.copy(routine = routine.copy(isFinished = true))
                val restarted = finished.copy(routine = finished.routine.copy(isFinished = false))

                summary.hasCycleProgress(today = today) shouldBe expected
                finished.hasCycleProgress(today = today) shouldBe false
                restarted.hasCycleProgress(today = today) shouldBe expected
            }
        }

        test("TC-c158cc96-e709-482b-afb6-a0a08e36efb6 기록은 시작 날짜와 같거나 그 뒤인 날짜에만 넣을 수 있다") {
            // 판정은 오늘을 입력으로 받지 않으므로 오늘 2026-11-13보다 앞이든 뒤든 같은 기준으로 판정한다.
            val detail = Arb.routineDetail(startDate = LocalDate(2026, 11, 10)).next()

            listOf(
                LocalDate(2026, 11, 9) to false,
                LocalDate(2026, 11, 10) to true,
                LocalDate(2026, 11, 12) to true,
                LocalDate(2026, 11, 13) to true,
                LocalDate(2026, 11, 18) to true,
                LocalDate(2099, 12, 31) to true,
            ).forEach { (date, expected) ->
                detail.canRecord(date = date) shouldBe expected
            }
        }

        test("TC-a1d370fb-a8c0-4aa6-b5dc-848d1d827ac4 오늘 날짜의 기록은 진행 중인 루틴에만 넣을 수 있다") {
            val today = LocalDate(2026, 11, 13)

            listOf(
                LocalDate(2026, 11, 10) to RoutineProgress.IN_PROGRESS,
                LocalDate(2026, 11, 13) to RoutineProgress.IN_PROGRESS,
                LocalDate(2026, 11, 14) to RoutineProgress.NOT_STARTED,
            ).forEach { (startDate, progress) ->
                val detail = Arb.routineDetail(startDate = startDate).next()

                detail.progress(today = today) shouldBe progress
                detail.canRecord(date = today) shouldBe (progress == RoutineProgress.IN_PROGRESS)
            }
        }

        test("TC-984607d8-7cef-4134-8ede-37d0a7fee226 시작 전인 루틴에도 시작 날짜와 같거나 그 뒤인 날짜에는 기록을 넣을 수 있다") {
            val notStarted = Arb.routineDetail(startDate = LocalDate(2026, 11, 20)).next()

            notStarted.progress(today = LocalDate(2026, 11, 13)) shouldBe RoutineProgress.NOT_STARTED
            notStarted.canRecord(date = LocalDate(2026, 11, 19)) shouldBe false
            notStarted.canRecord(date = LocalDate(2026, 11, 20)) shouldBe true
            notStarted.canRecord(date = LocalDate(2026, 11, 25)) shouldBe true
        }

        test("TC-3c883f41-9bbb-4b3e-b611-d4b439ba1ca4 완료 여부는 기록할 수 있는 날짜를 바꾸지 않는다") {
            listOf(
                Triple(LocalDate(2026, 11, 10), LocalDate(2026, 11, 9), false),
                Triple(LocalDate(2026, 11, 10), LocalDate(2026, 11, 10), true),
                Triple(LocalDate(2026, 11, 10), LocalDate(2026, 11, 13), true),
                Triple(LocalDate(2026, 11, 10), LocalDate(2026, 11, 18), true),
                Triple(LocalDate(2026, 11, 20), LocalDate(2026, 11, 13), false),
                Triple(LocalDate(2026, 11, 20), LocalDate(2026, 11, 20), true),
            ).forEach { (startDate, date, expected) ->
                val detail = Arb.routineDetail(startDate = startDate).next()

                listOf(false, true).forEach { isFinished ->
                    val routine = Arb.routine(isDeleted = false, detail = detail, isFinished = isFinished).next()

                    routine.detail.canRecord(date = date) shouldBe expected
                }
            }
        }

        test("TC-d501bfcb-b65e-4fc3-8460-1611469c275b 주기와 하는 요일은 기록할 수 있는 날짜를 바꾸지 않는다") {
            // 판정은 오늘을 입력으로 받지 않으므로 오늘이 하는 요일인지와 관계없이 같은 기준으로 판정한다.
            listOf(
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 14), true),
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 15), true),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 13), true),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 16), true),
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 9), false),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 8), false),
            ).forEach { (cycle, date, expected) ->
                val detail = Arb.routineDetail(startDate = LocalDate(2026, 11, 10), cycle = cycle).next()

                detail.canRecord(date = date) shouldBe expected
            }
        }
    })
