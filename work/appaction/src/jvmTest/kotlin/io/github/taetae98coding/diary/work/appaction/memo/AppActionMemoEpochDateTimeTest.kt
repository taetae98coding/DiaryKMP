package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone

class AppActionMemoEpochDateTimeTest :
    FunSpec({
        val seoul = TimeZone.of("Asia/Seoul")

        // 2026-01-31T00:00 Asia/Seoul
        val januaryThirtyFirst = 1_769_785_200_000L
        val hour = 3_600_000L
        val minute = 60_000L
        val day = 24 * hour

        test("날짜만 쓰는 기간은 시작일과 마지막 날의 0시로 바꾼다") {
            AppActionMemoDateTime(allDay = true, start = "2026-01-31", endInclusive = "2026-02-01").toEpochDateTime(timeZone = seoul) shouldBe
                AppActionMemoEpochDateTime(allDay = true, startEpochMillis = januaryThirtyFirst, endInclusiveEpochMillis = januaryThirtyFirst + day)
        }

        test("시간이 있는 기간은 그 시각으로 바꾼다") {
            AppActionMemoDateTime(allDay = false, start = "2026-01-31T09:30", endInclusive = "2026-01-31T18:00").toEpochDateTime(timeZone = seoul) shouldBe
                AppActionMemoEpochDateTime(
                    allDay = false,
                    startEpochMillis = januaryThirtyFirst + 9 * hour + 30 * minute,
                    endInclusiveEpochMillis = januaryThirtyFirst + 18 * hour,
                )
        }

        test("기간 입력이 모두 비어 있으면 기간을 정하지 않는다") {
            appActionMemoDateTimeInput(allDay = null, startEpochMillis = null, endInclusiveEpochMillis = null, timeZone = seoul) shouldBe null
        }

        test("하루 종일 기간 입력은 기기 시간대의 날짜로 바꾼다") {
            appActionMemoDateTimeInput(allDay = true, startEpochMillis = januaryThirtyFirst + 23 * hour, endInclusiveEpochMillis = januaryThirtyFirst + day, timeZone = seoul) shouldBe
                AppActionMemoDateTimeInput(allDay = true, start = "2026-01-31", endInclusive = "2026-02-01")
        }

        test("시간이 있는 기간 입력은 분 단위 시각으로 바꾼다") {
            appActionMemoDateTimeInput(
                allDay = false,
                startEpochMillis = januaryThirtyFirst + 9 * hour + 30 * minute + 59_999L,
                endInclusiveEpochMillis = januaryThirtyFirst + 18 * hour,
                timeZone = seoul,
            ) shouldBe AppActionMemoDateTimeInput(allDay = false, start = "2026-01-31T09:30", endInclusive = "2026-01-31T18:00")
        }

        test("하루 종일 여부 없이 날짜만 넣으면 데스크톱 연결과 같은 실패를 알린다") {
            val input = appActionMemoDateTimeInput(allDay = null, startEpochMillis = januaryThirtyFirst, endInclusiveEpochMillis = null, timeZone = seoul)

            input shouldBe AppActionMemoDateTimeInput(allDay = null, start = "2026-01-31T00:00", endInclusive = null)
            shouldThrow<AppActionException.InvalidArgument> { input?.toMemoDateTime() }.message shouldBe "'dateTime.allDay' is required."
        }

        test("기간 입력을 바꿨다가 되돌려도 같은 기간이다") {
            val dateTime = AppActionMemoDateTime(allDay = false, start = "2026-03-01T07:05", endInclusive = "2026-03-02T23:59")
            val epoch = dateTime.toEpochDateTime(timeZone = seoul)

            appActionMemoDateTimeInput(allDay = epoch.allDay, startEpochMillis = epoch.startEpochMillis, endInclusiveEpochMillis = epoch.endInclusiveEpochMillis, timeZone = seoul) shouldBe
                AppActionMemoDateTimeInput(allDay = false, start = dateTime.start, endInclusive = dateTime.endInclusive)
        }
    })
