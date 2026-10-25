package io.github.taetae98coding.diary.domain.qr.content

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.reflect.KClass

private val SEOUL: TimeZone = TimeZone.of("Asia/Seoul")

class QrActionTest :
    FunSpec({
        test("TC-ae5b2fc2-3b3b-45e7-ab39-d4e04f78cd11 QR 값의 포맷과 내용으로 실행 동작을 정한다") {
            val caseList: List<Pair<String, KClass<out QrAction>>> =
                listOf(
                    "https://example.com" to QrAction.OpenUrl::class,
                    "HTTP://EXAMPLE.COM" to QrAction.OpenUrl::class,
                    "tel:010" to QrAction.Call::class,
                    "mailto:a@example.com" to QrAction.SendEmail::class,
                    "mailto:?subject=Hi" to QrAction.SendEmail::class,
                    "SMSTO:010:hi" to QrAction.SendSms::class,
                    "sms:010?body=hi" to QrAction.SendSms::class,
                    "geo:37.5665,126.978" to QrAction.ShowLocation::class,
                    lines("BEGIN:VCARD", "EMAIL:a@example.com", "END:VCARD") to QrAction.AddContact::class,
                    lines("BEGIN:VEVENT", "DTSTART;VALUE=DATE:20261014", "END:VEVENT") to QrAction.AddEvent::class,
                    "WIFI:T:WEP;S:home;P:12345;;" to QrAction.JoinWifi::class,
                    "WIFI:S:cafe;;" to QrAction.JoinWifi::class,
                )

            caseList.forEach { (raw, type) ->
                withClue(raw) {
                    val action = qrActionOf(raw = raw, timeZone = SEOUL)

                    action?.let { it::class } shouldBe type
                }
            }
        }

        test("TC-ae5b2fc2-3b3b-45e7-ab39-d4e04f78cd11 실행 동작은 QR 값에서 읽은 내용을 담는다") {
            val caseList: List<Pair<String, QrAction>> =
                listOf(
                    "https://example.com/a?b=1" to QrAction.OpenUrl(url = "https://example.com/a?b=1"),
                    "tel:010-1234-5678" to QrAction.Call(phoneNumber = "010-1234-5678"),
                    "mailto:a@example.com?subject=Hi&body=Hello%20there" to QrAction.SendEmail(to = "a@example.com", subject = "Hi", body = "Hello there"),
                    "sms:010?body=hi" to QrAction.SendSms(phoneNumber = "010", message = "hi"),
                    "geo:37.5665,126.978" to QrAction.ShowLocation(coordinate = Coordinate(latitude = 37.5665, longitude = 126.978)),
                    "WIFI:T:WPA;S:home;P:password;H:true;;" to
                        QrAction.JoinWifi(ssid = "home", security = QrWifiSecurity.WPA, password = "password", isHidden = true),
                )

            caseList.forEach { (raw, expected) ->
                withClue(raw) { qrActionOf(raw = raw, timeZone = SEOUL) shouldBe expected }
            }
        }

        test("TC-ae5b2fc2-3b3b-45e7-ab39-d4e04f78cd11 연락처 실행 동작은 연락처 입력과 QR 값 전체를 담는다") {
            val raw =
                lines(
                    "BEGIN:VCARD",
                    "FN:홍길동",
                    "TEL:010",
                    "EMAIL:a@example.com",
                    "ORG:회사",
                    "ADR:;;세종대로;서울;;;",
                    "URL:https://example.com",
                    "END:VCARD",
                )

            qrActionOf(raw = raw, timeZone = SEOUL) shouldBe
                QrAction.AddContact(
                    name = "홍길동",
                    phoneNumber = "010",
                    email = "a@example.com",
                    company = "회사",
                    address = "세종대로 서울",
                    website = "https://example.com",
                    vCard = raw,
                )
        }

        test("TC-74728975-4ecd-4eed-a44b-f4bef5ac89ad 실행할 수 있는 조건을 만족하지 않으면 실행 동작이 없다") {
            val rawList =
                listOf(
                    "",
                    "hello",
                    " https://example.com",
                    "tel:",
                    "tel:   ",
                    "mailto:",
                    "SMSTO::hi",
                    "geo:91,0",
                    "geo:abc,127",
                    lines("BEGIN:VCARD", "VERSION:3.0", "END:VCARD"),
                    lines("BEGIN:VEVENT", "END:VEVENT"),
                    "WIFI:T:WPA;P:password;;",
                )

            rawList.forEach { raw ->
                withClue(raw) { qrActionOf(raw = raw, timeZone = SEOUL).shouldBeNull() }
            }
        }

        test("TC-a18051d4-fe9a-47e2-ae55-61fa4acd7ff4 넘기는 글자 값은 앞뒤 공백을 지우고 Wi-Fi 이름과 비밀번호는 그대로 둔다") {
            qrActionOf(raw = "SMSTO: 010 : hi ", timeZone = SEOUL) shouldBe QrAction.SendSms(phoneNumber = "010", message = "hi")
            qrActionOf(raw = "WIFI:S: home ;P: pass word ;;", timeZone = SEOUL) shouldBe
                QrAction.JoinWifi(ssid = " home ", security = QrWifiSecurity.NONE, password = " pass word ", isHidden = false)
        }

        test("TC-6e70a5d9-9249-4272-af24-31c5e5aab85c 일정의 기간은 QR 값의 시간대 표기로 해석한다") {
            val start = LocalDateTime(year = 2026, month = 10, day = 14, hour = 9, minute = 0)
            val end = LocalDateTime(year = 2026, month = 10, day = 14, hour = 10, minute = 0)
            val caseList: List<Pair<List<String>, QrEventSchedule?>> =
                listOf(
                    listOf("DTSTART:20261014T090000Z", "DTEND:20261014T100000Z") to
                        QrEventSchedule.DateTime(start = start.toInstant(TimeZone.UTC), end = end.toInstant(TimeZone.UTC)),
                    listOf("DTSTART;TZID=America/New_York:20261014T090000", "DTEND;TZID=America/New_York:20261014T100000") to
                        QrEventSchedule.DateTime(
                            start = start.toInstant(TimeZone.of("America/New_York")),
                            end = end.toInstant(TimeZone.of("America/New_York")),
                        ),
                    listOf("DTSTART:20261014T090000", "DTEND:20261014T100000") to
                        QrEventSchedule.DateTime(start = start.toInstant(SEOUL), end = end.toInstant(SEOUL)),
                    listOf("DTSTART;TZID=Unknown/Zone:20261014T090000", "DTEND;TZID=Unknown/Zone:20261014T100000") to
                        QrEventSchedule.DateTime(start = start.toInstant(SEOUL), end = end.toInstant(SEOUL)),
                    listOf("DTSTART;VALUE=DATE:20261014", "DTEND;VALUE=DATE:20261016") to
                        QrEventSchedule.AllDay(start = LocalDate(2026, 10, 14), endInclusive = LocalDate(2026, 10, 15)),
                    emptyList<String>() to null,
                )

            caseList.forEach { (periodLineList, expected) ->
                withClue(periodLineList) {
                    val raw = lines(listOf("BEGIN:VEVENT", "SUMMARY:회의") + periodLineList + "END:VEVENT")
                    val action = qrActionOf(raw = raw, timeZone = SEOUL)

                    action.shouldBeInstanceOf<QrAction.AddEvent>().schedule shouldBe expected
                }
            }
        }

        test("TC-a18e0a1c-fd01-4b30-8398-f52208db974a 일정 파일은 달력 묶음으로 감싸 담는다") {
            val event = lines("BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT")
            val calendar = lines("BEGIN:VCALENDAR", "BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT", "END:VCALENDAR")
            val caseList =
                listOf(
                    event to lines("BEGIN:VCALENDAR", "VERSION:2.0", "BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT", "END:VCALENDAR"),
                    calendar to calendar,
                )

            caseList.forEach { (raw, expected) ->
                withClue(raw) { qrActionOf(raw = raw, timeZone = SEOUL).shouldBeInstanceOf<QrAction.AddEvent>().calendarFile shouldBe expected }
            }
        }

        test("일정 QR 값이 줄바꿈으로 끝나도 달력 묶음 안에 빈 줄을 남기지 않는다") {
            val raw = lines("BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT") + "\r\n"

            qrActionOf(raw = raw, timeZone = SEOUL).shouldBeInstanceOf<QrAction.AddEvent>().calendarFile shouldBe
                lines("BEGIN:VCALENDAR", "VERSION:2.0", "BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT", "END:VCALENDAR")
        }
    })

private fun lines(vararg lineList: String): String = lines(lineList.toList())

private fun lines(lineList: List<String>): String = lineList.joinToString(separator = "\r\n")
