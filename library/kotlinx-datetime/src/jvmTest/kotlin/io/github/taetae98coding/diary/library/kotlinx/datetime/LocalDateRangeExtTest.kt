package io.github.taetae98coding.diary.library.kotlinx.datetime

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month

class LocalDateRangeExtTest : FunSpec() {
    init {
        test("기간이 서로 걸치면 겹친 것으로 본다") {
            (dateRange(from = 1, to = 5) overlaps dateRange(from = 3, to = 7)) shouldBe true
        }

        test("하루만 맞닿아도 겹친 것으로 본다") {
            (dateRange(from = 1, to = 5) overlaps dateRange(from = 5, to = 9)) shouldBe true
        }

        test("한 기간이 다른 기간을 품으면 겹친 것으로 본다") {
            (dateRange(from = 1, to = 9) overlaps dateRange(from = 4, to = 5)) shouldBe true
        }

        test("떨어진 기간은 겹치지 않은 것으로 본다") {
            (dateRange(from = 1, to = 5) overlaps dateRange(from = 6, to = 9)) shouldBe false
        }

        test("하루도 담지 않은 기간은 어떤 기간과도 겹치지 않는다") {
            val empty = dateRange(from = 5, to = 4)

            (empty overlaps dateRange(from = 1, to = 9)) shouldBe false
            (dateRange(from = 1, to = 9) overlaps empty) shouldBe false
            (empty overlaps empty) shouldBe false
        }

        test("겹침 판정은 두 기간의 순서를 바꿔도 같다") {
            val pairList =
                listOf(
                    dateRange(from = 1, to = 5) to dateRange(from = 3, to = 7),
                    dateRange(from = 1, to = 5) to dateRange(from = 6, to = 9),
                    dateRange(from = 1, to = 9) to dateRange(from = 4, to = 5),
                )

            pairList.forEach { (first, second) ->
                (first overlaps second) shouldBe (second overlaps first)
            }
        }
    }
}

private fun dateRange(
    from: Int,
    to: Int,
): LocalDateRange =
    LocalDateRange(
        start = LocalDate(year = 2026, month = Month.JULY, day = from),
        endInclusive = LocalDate(year = 2026, month = Month.JULY, day = to),
    )
