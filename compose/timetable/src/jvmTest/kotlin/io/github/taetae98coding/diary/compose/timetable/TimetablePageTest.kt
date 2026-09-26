package io.github.taetae98coding.diary.compose.timetable

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class TimetablePageTest :
    FunSpec({
        test("TC-TIMETABLE-DOMAIN-001 주간 형식의 표시 날짜는 지정한 날짜가 속한 주의 일요일부터 토요일까지다") {
            val week = date(month = 9, day = 20)..date(month = 9, day = 26)
            val caseList =
                listOf(
                    date(month = 9, day = 20) to week,
                    date(month = 9, day = 23) to week,
                    date(month = 9, day = 26) to week,
                    date(month = 10, day = 1) to date(month = 9, day = 27)..date(month = 10, day = 3),
                )

            caseList.forEach { (date, expected) ->
                TimetableState(type = TimetableType.WEEK, initialDate = date).currentDateRange shouldBe expected
            }
        }

        test("하루 형식의 첫 페이지는 1년 1월 1일이 속한 주의 일요일이다") {
            val firstDate = LocalDate(year = 0, month = 12, day = 31)

            TimetableType.DAY.pageOf(firstDate) shouldBe 0
            TimetableType.DAY.dateRangeAt(0) shouldBe firstDate..firstDate
        }

        test("1년 1월 1일이 속한 주는 주간 형식의 첫 페이지다") {
            val firstDate = LocalDate(year = 1, month = 1, day = 1)

            TimetableType.WEEK.pageOf(firstDate) shouldBe 0
            TimetableType.WEEK.dateRangeAt(0) shouldBe LocalDate(year = 0, month = 12, day = 31)..LocalDate(year = 1, month = 1, day = 6)
        }

        test("하루 형식의 다음 페이지는 다음 날이고 이전 페이지는 전날이다") {
            val page = TimetableType.DAY.pageOf(date(month = 9, day = 30))

            TimetableType.DAY.dateRangeAt(page) shouldBe date(month = 9, day = 30)..date(month = 9, day = 30)
            TimetableType.DAY.dateRangeAt(page + 1) shouldBe date(month = 10, day = 1)..date(month = 10, day = 1)
            TimetableType.DAY.dateRangeAt(page - 1) shouldBe date(month = 9, day = 29)..date(month = 9, day = 29)
        }

        test("주간 형식의 다음 페이지는 다음 주이고 이전 페이지는 이전 주다") {
            val page = TimetableType.WEEK.pageOf(date(month = 9, day = 23))

            TimetableType.WEEK.dateRangeAt(page + 1) shouldBe date(month = 9, day = 27)..date(month = 10, day = 3)
            TimetableType.WEEK.dateRangeAt(page - 1) shouldBe date(month = 9, day = 13)..date(month = 9, day = 19)
        }

        test("TC-TIMETABLE-DOMAIN-014 이동 범위보다 이른 날짜를 지정하면 이동 범위의 처음을 표시한다") {
            val earlyDate = LocalDate(year = 0, month = 12, day = 1)

            TimetableState(type = TimetableType.DAY, initialDate = earlyDate).currentDateRange shouldBe
                LocalDate(year = 0, month = 12, day = 31)..LocalDate(year = 0, month = 12, day = 31)
            TimetableState(type = TimetableType.WEEK, initialDate = earlyDate).currentDateRange shouldBe
                LocalDate(year = 0, month = 12, day = 31)..LocalDate(year = 1, month = 1, day = 6)
        }
    })

private fun date(
    month: Int,
    day: Int,
): LocalDate = LocalDate(year = 2026, month = month, day = day)
