package io.github.taetae98coding.diary.work.daily.memo

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours

private val SEOUL = TimeZone.of("Asia/Seoul")
private val EIGHT_AM = LocalTime(hour = 8, minute = 0)

class DailyMemoNotificationInstantTest :
    FunSpec({
        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-001 알림 발생 시각은 현재 시각을 기준으로 다음에 오는 오전 8시다") {
            val expectedByNow =
                mapOf(
                    LocalDateTime(2026, 9, 8, 0, 0) to LocalDateTime(2026, 9, 8, 8, 0),
                    LocalDateTime(2026, 9, 8, 7, 59) to LocalDateTime(2026, 9, 8, 8, 0),
                    LocalDateTime(2026, 9, 8, 8, 0) to LocalDateTime(2026, 9, 8, 8, 0),
                    LocalDateTime(2026, 9, 8, 8, 1) to LocalDateTime(2026, 9, 9, 8, 0),
                    LocalDateTime(2026, 9, 8, 23, 59) to LocalDateTime(2026, 9, 9, 8, 0),
                )

            expectedByNow.forEach { (now, expected) ->
                withClue("$now 에 예약하면 $expected 에 발생한다") {
                    nextDailyMemoNotificationInstant(
                        from = now.toInstant(SEOUL),
                        time = EIGHT_AM,
                        timeZone = SEOUL,
                    ) shouldBe expected.toInstant(SEOUL)
                }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-002 기기의 시간대가 바뀌면 바뀐 시간대의 오전 8시를 따른다") {
            listOf("Asia/Seoul", "UTC", "America/New_York").forEach { zoneId ->
                withClue("$zoneId 시간대에서는 그 시간대의 오전 8시에 발생한다") {
                    val timeZone = TimeZone.of(zoneId)
                    val now = LocalDateTime(2026, 9, 8, 0, 0).toInstant(timeZone)

                    nextDailyMemoNotificationInstant(from = now, time = EIGHT_AM, timeZone = timeZone) shouldBe
                        LocalDateTime(2026, 9, 8, 8, 0).toInstant(timeZone)
                }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-002 같은 절대 시각이라도 시간대가 다르면 발생 시각이 달라진다") {
            val now = LocalDateTime(2026, 9, 8, 0, 0).toInstant(TimeZone.UTC)

            nextDailyMemoNotificationInstant(from = now, time = EIGHT_AM, timeZone = TimeZone.UTC) shouldBe
                LocalDateTime(2026, 9, 8, 8, 0).toInstant(TimeZone.UTC)
            nextDailyMemoNotificationInstant(from = now, time = EIGHT_AM, timeZone = SEOUL) shouldBe
                LocalDateTime(2026, 9, 9, 8, 0).toInstant(SEOUL)
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-003 앱이 실행 중이 아닌 동안 지나간 오전 8시의 알림은 보정하지 않는다") {
            val now = LocalDateTime(2026, 9, 8, 10, 0).toInstant(SEOUL)

            nextDailyMemoNotificationInstant(from = now, time = EIGHT_AM, timeZone = SEOUL) shouldBe
                LocalDateTime(2026, 9, 9, 8, 0).toInstant(SEOUL)
        }

        test("예약까지 남은 시간은 현재 시각과 다음 발생 시각의 차이다") {
            val now = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL)

            dailyMemoNotificationDelay(from = now, time = EIGHT_AM, timeZone = SEOUL) shouldBe 2.hours
        }
    })
