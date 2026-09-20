package io.github.taetae98coding.diary.domain.memo.usecase

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

private val SEOUL = TimeZone.of("Asia/Seoul")
private val TODAY = LocalDate(2026, 9, 20)
private val TOMORROW = TODAY.plus(1, DateTimeUnit.DAY)

class DailyMemoNotificationPolicyTest :
    FunSpec({
        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017 미리 정하는 알림은 다음에 오는 오전 8시가 첫째 날이고 하루씩 이어지는 7개 날짜다") {
            listOf(
                LocalTime(hour = 7, minute = 59) to TODAY,
                LocalTime(hour = 8, minute = 0) to TODAY,
                LocalTime(hour = 8, minute = 1) to TOMORROW,
            ).forEach { (time, firstDate) ->
                val now = LocalDateTime(date = TODAY, time = time).toInstant(SEOUL)

                upcomingDailyMemoNotificationDateList(now = now, timeZone = SEOUL) shouldBe List(size = 7) { index -> firstDate.plus(index, DateTimeUnit.DAY) }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-002 첫째 날은 기기 시간대의 날짜로 정한다") {
            // 같은 절대 시각이 서울과 UTC에서는 9월 21일 오전 8시를 지났고, 로스앤젤레스에서는 9월 21일 오전 6시라 아직 지나지 않았다.
            val now = LocalDateTime(2026, 9, 21, 13, 0).toInstant(TimeZone.UTC)

            upcomingDailyMemoNotificationDateList(now = now, timeZone = SEOUL).first() shouldBe LocalDate(2026, 9, 22)
            upcomingDailyMemoNotificationDateList(now = now, timeZone = TimeZone.UTC).first() shouldBe LocalDate(2026, 9, 22)
            upcomingDailyMemoNotificationDateList(now = now, timeZone = TimeZone.of("America/Los_Angeles")).first() shouldBe LocalDate(2026, 9, 21)
        }
    })
