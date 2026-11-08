package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant

class AppActionEpochDateTest :
    FunSpec({
        test("날짜 입력은 시각이 속한 기기 시간대의 날짜로 바꾼다") {
            val timeZone = TimeZone.of("Asia/Seoul")
            val date = Arb.kotlinxLocalDate().next()
            val time = LocalTime(hour = Arb.int(0..23).next(), minute = Arb.int(0..59).next())
            val epochMillis = LocalDateTime(date = date, time = time).toInstant(timeZone).toEpochMilliseconds()

            appActionDate(epochMillis = epochMillis, timeZone = timeZone) shouldBe date.toString()
        }

        test("같은 시각도 시간대가 다르면 그 시간대의 날짜로 바꾼다") {
            val date = Arb.kotlinxLocalDate().next()
            // 서울 0시는 UTC로 전날 15시다.
            val epochMillis = date.atStartOfDayIn(TimeZone.of("Asia/Seoul")).toEpochMilliseconds()

            appActionDate(epochMillis = epochMillis, timeZone = TimeZone.UTC) shouldBe LocalDate.fromEpochDays(date.toEpochDays() - 1).toString()
        }

        test("날짜 결과는 그날 0시로 바꾸고, 다시 날짜로 바꾸면 같은 날짜가 된다") {
            val timeZone = TimeZone.of("America/New_York")
            val date = Arb.kotlinxLocalDate().next()

            val epochMillis = appActionDateEpochMillis(date = date.toString(), timeZone = timeZone)

            epochMillis shouldBe date.atStartOfDayIn(timeZone).toEpochMilliseconds()
            appActionDate(epochMillis = epochMillis, timeZone = timeZone) shouldBe date.toString()
        }
    })
