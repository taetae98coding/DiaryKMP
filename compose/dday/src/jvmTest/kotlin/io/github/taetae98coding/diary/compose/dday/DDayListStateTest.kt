package io.github.taetae98coding.diary.compose.dday

import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

class DDayListStateTest :
    FunSpec({
        test("기기의 날짜가 바뀌어도 오늘을 다시 정하기 전에는 처음 오늘을 유지한다") {
            val initialToday = Arb.kotlinxLocalDate().next()
            var now: Instant = initialToday.atStartOfDayIn(TimeZone.currentSystemDefault())
            val clock = mockk<Clock>()
            every { clock.now() } answers { now }
            val state = DDayListState(initialToday = initialToday, clock = clock)

            now = initialToday.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault())

            state.today shouldBe initialToday
        }

        test("오늘을 다시 정하면 그때의 기기 날짜가 오늘이 된다") {
            val initialToday = Arb.kotlinxLocalDate().next()
            val nextDay = initialToday.plus(1, DateTimeUnit.DAY)
            val clock = mockk<Clock>()
            every { clock.now() } returns nextDay.atStartOfDayIn(TimeZone.currentSystemDefault())
            val state = DDayListState(initialToday = initialToday, clock = clock)

            state.updateToday()

            state.today shouldBe nextDay
        }
    })
