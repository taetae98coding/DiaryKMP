package io.github.taetae98coding.diary.compose.dday

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.domain.dday.count.count
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 디데이 테스트 케이스의 카드 문구를 한국어 환경에서 카드가 쓰는 문구로 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class DDayCardTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ed4ff141-dc01-4e74-8b6f-d7e85af490ae 기준일 전에는 남은 날 수로 정하고 1일로 세기의 영향을 받지 않는다`() {
        val today = LocalDate(2026, 1, 1)
        val rowList =
            listOf(
                LocalDate(2026, 1, 2) to "D-1",
                LocalDate(2026, 1, 21) to "D-20",
                LocalDate(2026, 2, 10) to "D-40(1개월)",
                LocalDate(2027, 2, 5) to "D-400(1년 1개월)",
            ).flatMap { (baseDate, text) ->
                listOf(true, false).map { countsBaseDay -> CardRow(Arb.ddayDetail(baseDate = baseDate, countsBaseDay = countsBaseDay).next(), today, text) }
            }

        assertCardText(rowList)
    }

    @Test
    fun `TC-9c8543b9-cea4-4da8-941c-6c4f1fe7216a 오늘이 기준일이면 카드 문구는 D-Day다`() {
        val date = LocalDate(2026, 1, 1)

        assertCardText(listOf(true, false).map { countsBaseDay -> CardRow(Arb.ddayDetail(baseDate = date, countsBaseDay = countsBaseDay).next(), date, "D-Day") })
    }

    @Test
    fun `TC-c6f04c36-cf45-466f-bb9e-d83c1eff6920 기준일 뒤에는 지난 날 수로 정하고 1일로 세기가 켜지면 1을 더한다`() {
        assertCardText(
            listOf(
                row(LocalDate(2026, 1, 1), LocalDate(2026, 1, 2), false, "1일"),
                row(LocalDate(2026, 1, 1), LocalDate(2026, 1, 2), true, "2일"),
                row(LocalDate(2026, 1, 1), LocalDate(2026, 1, 25), true, "25일"),
                row(LocalDate(2025, 1, 15), LocalDate(2026, 1, 15), false, "365일(1년)"),
                row(LocalDate(2025, 1, 15), LocalDate(2026, 1, 15), true, "366일(1년)"),
                row(LocalDate(2025, 1, 15), LocalDate(2026, 3, 20), false, "429일(1년 2개월)"),
                row(LocalDate(2025, 1, 15), LocalDate(2026, 3, 20), true, "430일(1년 2개월)"),
            ),
        )
    }

    @Test
    fun `TC-a238df1c-789c-4ae6-8c83-bacdc2f5a0e1 앞선 날짜의 일과 같은 일에 이르러야 한 달을 채운다`() {
        assertCardText(
            listOf(
                row(LocalDate(2026, 1, 15), LocalDate(2026, 2, 14), false, "30일"),
                row(LocalDate(2026, 1, 15), LocalDate(2026, 2, 15), false, "31일(1개월)"),
                row(LocalDate(2026, 2, 14), LocalDate(2026, 1, 15), false, "D-30"),
                row(LocalDate(2026, 2, 15), LocalDate(2026, 1, 15), false, "D-31(1개월)"),
            ),
        )
    }

    @Test
    fun `TC-4c1da263-ab50-4c39-a330-fdcabfc8de97 앞선 날짜의 일이 그 달에 없으면 다음 달 1일에 채운다`() {
        assertCardText(
            listOf(
                row(LocalDate(2026, 1, 31), LocalDate(2026, 2, 28), false, "28일"),
                row(LocalDate(2026, 1, 31), LocalDate(2026, 3, 1), false, "29일(1개월)"),
                row(LocalDate(2028, 1, 31), LocalDate(2028, 2, 29), false, "29일"),
                row(LocalDate(2028, 1, 31), LocalDate(2028, 3, 1), false, "30일(1개월)"),
                row(LocalDate(2024, 2, 29), LocalDate(2025, 2, 28), false, "365일(11개월)"),
                row(LocalDate(2024, 2, 29), LocalDate(2025, 3, 1), false, "366일(1년)"),
                row(LocalDate(2026, 2, 28), LocalDate(2026, 1, 31), false, "D-28"),
                row(LocalDate(2026, 3, 1), LocalDate(2026, 1, 31), false, "D-29(1개월)"),
            ),
        )
    }

    @Test
    fun `TC-f334f30d-6c0d-49bd-aa81-03399ab67ac3 12개월을 채우면 1년으로 바꿔 세고 0인 단위는 쓰지 않는다`() {
        val baseDate = LocalDate(2025, 3, 10)

        assertCardText(
            listOf(
                row(baseDate, LocalDate(2026, 3, 9), false, "364일(11개월)"),
                row(baseDate, LocalDate(2026, 3, 10), false, "365일(1년)"),
            ),
        )
    }

    @Test
    fun `TC-3ca69a0a-dbe6-47bd-a9ff-67e6981d8046 채운 년과 개월은 1일로 세기와 달력에 없는 날짜 처리의 영향을 받지 않는다`() {
        val baseDate = LocalDate(2026, 1, 31)
        val today = LocalDate(2026, 3, 1)
        val rowList =
            listOf(false to "29일(1개월)", true to "30일(1개월)").flatMap { (countsBaseDay, text) ->
                DDayOverflowPolicy.entries.map { policy ->
                    CardRow(Arb.ddayDetail(baseDate = baseDate, countsBaseDay = countsBaseDay, overflowPolicy = policy).next(), today, text)
                }
            }

        assertCardText(rowList)
    }

    @Test
    fun `TC-d64b7767-54d4-441e-9021-6bb8fb6bceac 카드 문구는 종료일과 반복 간격의 영향을 받지 않는다`() {
        val baseDate = LocalDate(2025, 1, 15)
        val today = LocalDate(2026, 3, 20)
        val yearly = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12))
        val rowList =
            listOf(
                emptySet<DateTimeUnit.DateBased>() to null,
                yearly to null,
                yearly to LocalDate(2025, 6, 1),
                emptySet<DateTimeUnit.DateBased>() to baseDate,
            ).map { (repeats, endDate) ->
                CardRow(Arb.ddayDetail(baseDate = baseDate, countsBaseDay = true, repeats = repeats, endDate = endDate).next(), today, "430일(1년 2개월)")
            }

        assertCardText(rowList)
    }

    @Test
    fun `TC-717b035d-b955-4e16-81af-df80e79e05b1 오늘이 바뀌면 카드 문구는 바뀐 오늘로 다시 정해진다`() {
        val detail = Arb.ddayDetail(baseDate = LocalDate(2026, 1, 1), countsBaseDay = false).next()

        assertCardText(
            listOf(
                CardRow(detail, LocalDate(2026, 1, 1), "D-Day"),
                CardRow(detail, LocalDate(2027, 6, 30), "545일(1년 5개월)"),
            ),
        )
    }

    private fun row(
        baseDate: LocalDate,
        today: LocalDate,
        countsBaseDay: Boolean,
        text: String,
    ): CardRow = CardRow(detail = Arb.ddayDetail(baseDate = baseDate, countsBaseDay = countsBaseDay).next(), today = today, text = text)

    private fun assertCardText(rowList: List<CardRow>) {
        var textList: List<String> = emptyList()

        composeRule.setContent {
            textList = rowList.map { row -> row.detail.count(today = row.today).toCardText() }
        }
        composeRule.waitForIdle()

        rowList.zip(textList).forEach { (row, text) -> (row to text) shouldBe (row to row.text) }
    }

    private data class CardRow(
        val detail: DDayDetail,
        val today: LocalDate,
        val text: String,
    )
}
