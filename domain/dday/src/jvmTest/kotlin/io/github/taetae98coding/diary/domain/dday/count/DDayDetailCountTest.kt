package io.github.taetae98coding.diary.domain.dday.count

import io.github.taetae98coding.diary.core.model.dday.DDayCount
import io.github.taetae98coding.diary.core.model.dday.DDayPeriod
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// 추가 테스트: 카드 문구의 바탕이 되는 계산의 경계를 확인한다. 문구 자체는 compose/dday의 DDayCardTextTest가 확인한다.
class DDayDetailCountTest :
    FunSpec({
        test("오늘이 기준일이면 기준일을 1일로 세기와 관계없이 당일이다") {
            val baseDate = Arb.kotlinxLocalDate().next()

            listOf(true, false).forEach { countsBaseDay ->
                Arb.ddayDetail(baseDate = baseDate, countsBaseDay = countsBaseDay).next().count(today = baseDate) shouldBe DDayCount.Today
            }
        }

        test("기준일 하루 전과 하루 뒤는 남은 날 1과 지난 날 1 또는 2다") {
            val baseDate = LocalDate(2026, 1, 1)

            Arb.ddayDetail(baseDate = baseDate).next().count(today = baseDate.minus(1, DateTimeUnit.DAY)) shouldBe
                DDayCount.Upcoming(remainingDays = 1, period = DDayPeriod(years = 0, months = 0))
            Arb.ddayDetail(baseDate = baseDate, countsBaseDay = false).next().count(today = baseDate.plus(1, DateTimeUnit.DAY)) shouldBe
                DDayCount.Passed(passedDays = 1, period = DDayPeriod(years = 0, months = 0))
            Arb.ddayDetail(baseDate = baseDate, countsBaseDay = true).next().count(today = baseDate.plus(1, DateTimeUnit.DAY)) shouldBe
                DDayCount.Passed(passedDays = 2, period = DDayPeriod(years = 0, months = 0))
        }

        test("남은 날의 년과 개월은 지난 날을 거꾸로 센 값과 같다") {
            val baseDate = LocalDate(2026, 1, 31)
            val today = baseDate.plus(Arb.int(1..3_650).next(), DateTimeUnit.DAY)
            val passed = Arb.ddayDetail(baseDate = baseDate).next().count(today = today) as DDayCount.Passed
            val upcoming = Arb.ddayDetail(baseDate = today).next().count(today = baseDate) as DDayCount.Upcoming

            upcoming.period shouldBe passed.period
        }

        test("오래된 기준일도 년과 개월을 12개월 단위로 나눠 센다") {
            Arb.ddayDetail(baseDate = LocalDate(1900, 1, 1), countsBaseDay = false).next().count(today = LocalDate(2026, 3, 1)) shouldBe
                DDayCount.Passed(passedDays = LocalDate(1900, 1, 1).daysUntilForTest(LocalDate(2026, 3, 1)), period = DDayPeriod(years = 126, months = 2))
        }
    })

private fun LocalDate.daysUntilForTest(other: LocalDate): Int = (other.toEpochDays() - toEpochDays()).toInt()
