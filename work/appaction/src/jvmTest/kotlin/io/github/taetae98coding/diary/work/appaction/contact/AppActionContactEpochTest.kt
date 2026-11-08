package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone

class AppActionContactEpochTest :
    FunSpec({
        val seoul = TimeZone.of("Asia/Seoul")

        // 2026-01-31T00:00 Asia/Seoul
        val januaryThirtyFirst = 1_769_785_200_000L
        val hour = 3_600_000L

        test("생일 날짜는 기기 시간대의 그날 0시로 바꾼다") {
            appActionContactBirthdayEpochMillis(date = "2026-01-31", timeZone = seoul) shouldBe januaryThirtyFirst
        }

        test("생일 입력은 기기 시간대의 날짜로 읽고, 비어 있으면 생일을 담지 않는다") {
            appActionContactBirthdayArgument(epochMillis = januaryThirtyFirst + 23 * hour, timeZone = seoul) shouldBe "2026-01-31"
            appActionContactBirthdayArgument(epochMillis = null, timeZone = seoul) shouldBe null
        }

        test("키 입력은 데스크톱 연결과 같은 글로 바꿔 같은 검사를 거친다") {
            appActionContactHeightArgument(heightCm = 172.5) shouldBe "172.5"
            appActionContactHeightArgument(heightCm = 180.0) shouldBe "180.0"
            appActionContactHeightArgument(heightCm = null) shouldBe null
            shouldThrow<AppActionException.InvalidArgument> { appActionContactHeightArgument(heightCm = 172.55).toContactHeightArgumentOrNull() }.message shouldBe
                "'heightCm' must be centimeters with up to three digits and one decimal place, like \"172.5\"."
        }
    })
