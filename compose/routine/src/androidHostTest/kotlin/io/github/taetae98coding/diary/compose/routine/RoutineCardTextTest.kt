package io.github.taetae98coding.diary.compose.routine

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.domain.routine.progress.progress
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 루틴 테스트 케이스의 카드 문구를 한국어 환경에서 카드가 쓰는 문구로 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class RoutineCardTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-5a8c94b7-944a-4098-bd91-5abc97871ece 진행 중이고 목표량이 있으면 주기 이름, 합계와 목표량, 달성률이다`() {
        assertCardText(
            listOf(
                inProgressRow(RoutineCycle.DAILY, "ml", 2000, 1500, TODAY_1500_OF_2000_ML_TEXT, 75),
                inProgressRow(RoutineCycle.WEEKLY, "회", 3, 2, "이번 주 2 / 3회", 66),
                inProgressRow(RoutineCycle.MONTHLY, "m", 20_000, 12_000, "이번 달 12000 / 20000m", 60),
                inProgressRow(RoutineCycle.DAILY, "ml", 2000, 0, "오늘 0 / 2000ml", 0),
                inProgressRow(RoutineCycle.DAILY, "ml", 2000, 2500, "오늘 2500 / 2000ml", 100),
            ),
        )
    }

    @Test
    fun `TC-982f0c46-607d-4650-a6a5-d7df1024100f 진행 중이고 목표량이 없으면 주기 이름과 합계이고 달성률은 없다`() {
        assertCardText(
            listOf(
                inProgressRow(RoutineCycle.MONTHLY, "m", null, 12_000, "이번 달 12000m", null),
                inProgressRow(RoutineCycle.DAILY, "회", null, 0, "오늘 0회", null),
                inProgressRow(RoutineCycle.WEEKLY, "ml", null, 1500, "이번 주 1500ml", null),
            ),
        )
    }

    @Test
    fun `TC-2f9e855f-abab-4459-8673-5d664684d64e 시작 전이면 목표량과 기록과 관계없이 시작 전이다`() {
        assertCardText(
            listOf<Pair<Int?, Long>>(null to 0, 2000 to 0, 2000 to 500, null to 500).map { (goalAmount, cycleAmount) ->
                CardRow(
                    summary =
                        summary(
                            startDate = LocalDate(2026, 11, 20),
                            cycle = RoutineCycle.MONTHLY,
                            unit = "ml",
                            goalAmount = goalAmount,
                            cycleAmount = cycleAmount,
                        ),
                    today = TODAY,
                    cardText = RoutineCardText(isFinished = false, progress = RoutineProgress.NOT_STARTED, text = "시작 전", achievementRate = null, achievementRateText = null),
                )
            },
        )
    }

    @Test
    fun `TC-becafe28-4267-4e23-9396-08b9fca0cd41 완료된 루틴은 진행 상태, 목표량과 기록과 관계없이 완료다`() {
        assertCardText(
            listOf<Triple<LocalDate, Int?, Long>>(
                Triple(LocalDate(2026, 11, 1), null, 0),
                Triple(LocalDate(2026, 11, 1), 2000, 1500),
                Triple(LocalDate(2026, 11, 1), 2000, 2000),
                Triple(LocalDate(2026, 11, 1), null, 2000),
                Triple(LocalDate(2026, 11, 20), null, 0),
                Triple(LocalDate(2026, 11, 20), 2000, 500),
            ).map { (startDate, goalAmount, cycleAmount) ->
                val summary =
                    summary(
                        startDate = startDate,
                        cycle = RoutineCycle.MONTHLY,
                        unit = "ml",
                        goalAmount = goalAmount,
                        cycleAmount = cycleAmount,
                        isFinished = true,
                    )

                CardRow(
                    summary = summary,
                    today = TODAY,
                    cardText =
                        RoutineCardText(
                            isFinished = true,
                            progress = summary.routine.detail.progress(today = TODAY),
                            text = "완료",
                            achievementRate = null,
                            achievementRateText = null,
                        ),
                )
            },
        )
    }

    @Test
    fun `TC-2652e6ab-e46c-4e3b-ab40-6b320278a02c 완료하면 완료가 되고 다시 시작하면 완료 전 문구로 돌아간다`() {
        val rowList =
            listOf(
                inProgressRow(RoutineCycle.DAILY, "ml", 2000, 1500, TODAY_1500_OF_2000_ML_TEXT, 75),
                inProgressRow(RoutineCycle.MONTHLY, "m", null, 12_000, "이번 달 12000m", null),
                CardRow(
                    summary = summary(startDate = LocalDate(2026, 11, 20), cycle = RoutineCycle.DAILY, unit = "회", goalAmount = null, cycleAmount = 0),
                    today = TODAY,
                    cardText = RoutineCardText(isFinished = false, progress = RoutineProgress.NOT_STARTED, text = "시작 전", achievementRate = null, achievementRateText = null),
                ),
            )
        var finishedList: List<RoutineCardText> = emptyList()
        var restartedList: List<RoutineCardText> = emptyList()

        composeRule.setContent {
            finishedList = rowList.map { row -> row.summary.copy(routine = row.summary.routine.copy(isFinished = true)).toCardText(today = row.today) }
            restartedList = rowList.map { row -> row.summary.copy(routine = row.summary.routine.copy(isFinished = false)).toCardText(today = row.today) }
        }
        composeRule.waitForIdle()

        finishedList.map { cardText -> Triple(cardText.text, cardText.achievementRate, cardText.achievementRateText) } shouldBe rowList.map { Triple("완료", null, null) }
        restartedList shouldBe rowList.map { row -> row.cardText }
    }

    @Test
    fun `TC-535c4071-5f2e-4ef7-ad46-72191b97513d 단위를 바꾼 루틴은 같은 양의 합계를 바뀐 단위의 카드 문구와 날짜별 합계 문구로 쓴다`() {
        val rowList =
            listOf(
                Triple("회", "오늘 1500회", "1500회"),
                Triple("m", "오늘 1500m", "1500m"),
                Triple("L", "오늘 1500L", "1500L"),
                Triple("", "오늘 1500", "1500"),
            )
        var textList: List<Pair<String, String>> = emptyList()

        composeRule.setContent {
            textList =
                rowList.map { (unit, _, _) ->
                    val summary = summary(startDate = TODAY, cycle = RoutineCycle.DAILY, unit = unit, goalAmount = null, cycleAmount = 1500)

                    summary.toCardText(today = TODAY).text to routineAmountText(amount = summary.cycleAmount, unit = summary.routine.detail.unit)
                }
        }
        composeRule.waitForIdle()

        textList shouldBe rowList.map { (_, cardText, amountText) -> cardText to amountText }
    }

    @Test
    fun `TC-a66b7b0c-556c-4a6c-b3e9-5d8bc6440c41 평일만과 주말만 루틴의 카드 문구는 하는 요일이 아닌 날에도 매일 루틴과 같다`() {
        assertCardText(
            listOf(
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 13), 2000),
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 14), 2000),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 14), 2000),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 13), 2000),
                Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 11, 15), null),
                Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 11, 16), null),
            ).map { (cycle, today, goalAmount) ->
                val daily = summary(startDate = LocalDate(2026, 1, 1), cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = goalAmount, cycleAmount = 1500)
                val hasGoal = goalAmount != null

                CardRow(
                    summary = daily.copy(routine = daily.routine.copy(detail = daily.routine.detail.copy(cycle = cycle))),
                    today = today,
                    cardText =
                        RoutineCardText(
                            isFinished = false,
                            progress = RoutineProgress.IN_PROGRESS,
                            text = if (hasGoal) TODAY_1500_OF_2000_ML_TEXT else "오늘 1500ml",
                            achievementRate = 75.takeIf { hasGoal },
                            achievementRateText = "75%".takeIf { hasGoal },
                        ),
                )
            },
        )
    }

    @Test
    fun `TC-731d6bde-1311-45ee-80b1-7605c8a6708f 평일만과 주말만 루틴의 주기 이름은 하는 요일이 아닌 날에도 오늘이다`() {
        val rowList =
            listOf(
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 13),
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 14),
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 15),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 14),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 13),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 16),
            )
        var textList: List<String> = emptyList()

        composeRule.setContent {
            textList =
                rowList.map { (cycle, today) ->
                    summary(startDate = LocalDate(2026, 1, 1), cycle = cycle, unit = "회", goalAmount = null, cycleAmount = 0).toCardText(today = today).text
                }
        }
        composeRule.waitForIdle()

        textList shouldBe rowList.map { "오늘 0회" }
    }

    @Test
    fun `TC-f49840de-0419-45c4-961d-ad96c83997fc 단위가 비어 있는 루틴의 카드 문구는 합계와 목표량을 숫자만으로 쓴다`() {
        assertCardText(
            listOf(
                inProgressRow(RoutineCycle.DAILY, "", 3, 2, "오늘 2 / 3", 66),
                inProgressRow(RoutineCycle.WEEKLY, "", 3, 0, "이번 주 0 / 3", 0),
                inProgressRow(RoutineCycle.DAILY, "", null, 0, "오늘 0", null),
                inProgressRow(RoutineCycle.MONTHLY, "", null, 12, "이번 달 12", null),
            ),
        )
    }

    private fun inProgressRow(
        cycle: RoutineCycle,
        unit: String,
        goalAmount: Int?,
        cycleAmount: Long,
        text: String,
        achievementRate: Int?,
    ): CardRow =
        CardRow(
            summary =
                summary(
                    startDate = TODAY,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    cycleAmount = cycleAmount,
                ),
            today = TODAY,
            cardText =
                RoutineCardText(
                    isFinished = false,
                    progress = RoutineProgress.IN_PROGRESS,
                    text = text,
                    achievementRate = achievementRate,
                    achievementRateText = achievementRate?.let { rate -> "$rate%" },
                ),
        )

    private fun assertCardText(rowList: List<CardRow>) {
        var cardTextList: List<RoutineCardText> = emptyList()

        composeRule.setContent {
            cardTextList = rowList.map { row -> row.summary.toCardText(today = row.today) }
        }
        composeRule.waitForIdle()

        rowList.zip(cardTextList).forEach { (row, cardText) -> (row to cardText) shouldBe (row to row.cardText) }
    }

    private data class CardRow(
        val summary: RoutineSummary,
        val today: LocalDate,
        val cardText: RoutineCardText,
    )

    private companion object {
        val TODAY = LocalDate(2026, 11, 13)
        const val TODAY_1500_OF_2000_ML_TEXT = "오늘 1500 / 2000ml"

        fun summary(
            startDate: LocalDate,
            cycle: RoutineCycle,
            unit: String,
            goalAmount: Int?,
            cycleAmount: Long,
            isFinished: Boolean = false,
        ): RoutineSummary =
            RoutineSummary(
                routine =
                    Arb
                        .routine(
                            isDeleted = false,
                            detail = Arb.routineDetail(startDate = startDate, cycle = cycle, goalAmount = goalAmount).next().copy(unit = unit),
                            isFinished = isFinished,
                        ).next(),
                cycleAmount = cycleAmount,
            )
    }
}
