package io.github.taetae98coding.diary.domain.qr.content

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class QrContentRawTest :
    FunSpec({
        test("TC-QR-ADD-DOMAIN-026 QR 값의 시작으로 포맷을 알아본다") {
            val caseList =
                listOf(
                    crlf("begin:vcard", "FN:a", "END:VCARD") to QrFormat.CONTACT,
                    crlf("BEGIN:VCALENDAR", "BEGIN:VEVENT", "END:VEVENT", "END:VCALENDAR") to QrFormat.EVENT,
                    crlf("BEGIN:VEVENT", "END:VEVENT") to QrFormat.EVENT,
                    crlf("BEGIN:VCALENDAR", "END:VCALENDAR") to QrFormat.TEXT,
                    "wifi:S:home;;" to QrFormat.WIFI,
                    "GEO:37.5,127" to QrFormat.LOCATION,
                    "mailto:a@b.c" to QrFormat.EMAIL,
                    "tel:010" to QrFormat.PHONE,
                    "SMSTO:010:hi" to QrFormat.SMS,
                    "sms:010?body=hi" to QrFormat.SMS,
                    "https://example.com" to QrFormat.URL,
                    "http://a.com\nb" to QrFormat.TEXT,
                    " WIFI:S:home;;" to QrFormat.TEXT,
                    "hello" to QrFormat.TEXT,
                )

            caseList.forEach { (raw, expected) ->
                withClue("QR 값=$raw") {
                    detectQrFormat(raw) shouldBe expected
                }
            }
        }

        test("TC-QR-ADD-DOMAIN-027 그 포맷의 값에서 포맷의 입력을 읽는다") {
            val caseList =
                listOf(
                    Triple(
                        QrFormat.CONTACT,
                        crlf("BEGIN:VCARD", "N:홍;길동;;;", "item1.TEL;TYPE=CELL:010", "TEL:011", "ORG:회사;부서", "ADR:;;서울;강남;;;", "END:VCARD"),
                        QrContent.Contact(name = "홍 길동", phoneNumber = "010", company = "회사", address = "서울 강남"),
                    ),
                    Triple(QrFormat.CONTACT, crlf("BEGIN:VCARD", """FN:a\,b""", "NOTE:x", "END:VCARD"), QrContent.Contact(name = "a,b")),
                    Triple(
                        QrFormat.WIFI,
                        """WIFI:T:WPA2;S:a\;b;P:pw;;""",
                        QrContent.Wifi(ssid = "a;b", security = QrWifiSecurity.WPA, password = "pw", isHidden = false),
                    ),
                    Triple(QrFormat.WIFI, "WIFI:S:home;;", QrContent.Wifi(ssid = "home", security = QrWifiSecurity.NONE)),
                    Triple(QrFormat.LOCATION, "geo:37.5,127,30;u=10?q=cafe", QrContent.Location(latitude = "37.5", longitude = "127")),
                    Triple(
                        QrFormat.EMAIL,
                        "mailto:a@b.c?cc=x@y.z&subject=%EC%95%88%EB%85%95&body=x%0D%0Ay",
                        QrContent.Email(to = "a@b.c", subject = "안녕", body = "x\ny"),
                    ),
                    Triple(QrFormat.PHONE, "TEL:+82-10", QrContent.Phone(phoneNumber = "+82-10")),
                    Triple(QrFormat.SMS, "SMSTO:010:a:b", QrContent.Sms(phoneNumber = "010", message = "a:b")),
                    Triple(QrFormat.SMS, "sms:010?body=hi%20there", QrContent.Sms(phoneNumber = "010", message = "hi there")),
                    Triple(
                        QrFormat.EVENT,
                        crlf(
                            "BEGIN:VCALENDAR",
                            "BEGIN:VEVENT",
                            "SUMMARY:회의",
                            "DTSTART;TZID=Asia/Seoul:20260927T140000",
                            "DTEND;TZID=Asia/Seoul:20260927T153000",
                            "END:VEVENT",
                            "END:VCALENDAR",
                        ),
                        QrContent.Event(
                            title = "회의",
                            period =
                                QrEventPeriod.DateTime(
                                    start = LocalDateTime(year = 2026, month = 9, day = 27, hour = 14, minute = 0),
                                    endInclusive = LocalDateTime(year = 2026, month = 9, day = 27, hour = 15, minute = 30),
                                ),
                        ),
                    ),
                    Triple(
                        QrFormat.EVENT,
                        crlf("BEGIN:VEVENT", "SUMMARY:여행", "DTSTART;VALUE=DATE:20260927", "DTEND;VALUE=DATE:20260929", "END:VEVENT"),
                        QrContent.Event(title = "여행", period = QrEventPeriod.AllDay(start = LocalDate(2026, 9, 27), endInclusive = LocalDate(2026, 9, 28))),
                    ),
                    Triple(
                        QrFormat.EVENT,
                        crlf("BEGIN:VEVENT", "SUMMARY:메모", "DESCRIPTION:a", " b", "END:VEVENT"),
                        QrContent.Event(title = "메모", description = "ab", period = null),
                    ),
                )

            caseList.forEach { (format, raw, expected) ->
                withClue("포맷=$format, QR 값=$raw") {
                    format.readContent(raw) shouldBe expected
                }
            }
        }

        test("TC-QR-ADD-DOMAIN-028 그 포맷의 값에서 입력을 고치면 그 입력에 해당하는 부분만 바꾼다") {
            val contactPhone = crlf("BEGIN:VCARD", "FN:a", "N:a", "item1.TEL;TYPE=CELL:010", "NOTE:x", "END:VCARD")
            val contactNote = crlf("BEGIN:VCARD", "FN:a", "NOTE:x", "END:VCARD")
            val contactTel = crlf("BEGIN:VCARD", "FN:a", "TEL:010", "END:VCARD")
            val contactName = crlf("BEGIN:VCARD", "N:a;b;;;", "FN:a b", "END:VCARD")
            val eventLocation = crlf("BEGIN:VEVENT", "SUMMARY:a", "RRULE:FREQ=WEEKLY", "END:VEVENT")
            val eventPeriod = crlf("BEGIN:VEVENT", "SUMMARY:a", "DTSTART:20260927T140000", "DTEND:20260927T150000", "END:VEVENT")
            val caseList =
                listOf(
                    Triple(contactPhone, { content: QrContent.Contact -> content.copy(phoneNumber = "011") }, crlf("BEGIN:VCARD", "FN:a", "N:a", "item1.TEL;TYPE=CELL:011", "NOTE:x", "END:VCARD")),
                    Triple(contactNote, { content: QrContent.Contact -> content.copy(company = "c") }, crlf("BEGIN:VCARD", "FN:a", "NOTE:x", "ORG:c", "END:VCARD")),
                    Triple(contactTel, { content: QrContent.Contact -> content.copy(phoneNumber = "") }, crlf("BEGIN:VCARD", "FN:a", "END:VCARD")),
                    Triple(contactName, { content: QrContent.Contact -> content.copy(name = "c") }, crlf("BEGIN:VCARD", "N:c", "FN:c", "END:VCARD")),
                )
            caseList.forEach { (raw, edit, expected) -> assertPatch(QrFormat.CONTACT, raw, edit, expected) }

            assertPatch(QrFormat.WIFI, "WIFI:T:WPA;S:home;P:pw;;", { content: QrContent.Wifi -> content.copy(security = QrWifiSecurity.NONE) }, "WIFI:T:nopass;S:home;;")
            assertPatch(QrFormat.WIFI, "WIFI:S:home;;", { content: QrContent.Wifi -> content.copy(isHidden = true) }, "WIFI:S:home;H:true;;")
            assertPatch(QrFormat.LOCATION, "geo:37.5,127,30;u=10?q=cafe", { content: QrContent.Location -> content.copy(latitude = "36") }, "geo:36,127,30;u=10?q=cafe")
            assertPatch(QrFormat.EMAIL, "mailto:a@b.c?cc=x@y.z&body=hi", { content: QrContent.Email -> content.copy(subject = "yo") }, "mailto:a@b.c?cc=x@y.z&body=hi&subject=yo")
            assertPatch(QrFormat.EMAIL, "mailto:a@b.c?cc=x@y.z&body=hi", { content: QrContent.Email -> content.copy(body = "") }, "mailto:a@b.c?cc=x@y.z")
            assertPatch(QrFormat.PHONE, "TEL:010", { content: QrContent.Phone -> content.copy(phoneNumber = "011") }, "TEL:011")
            assertPatch(QrFormat.SMS, "sms:010?body=hi", { content: QrContent.Sms -> content.copy(phoneNumber = "011") }, "sms:011?body=hi")
            assertPatch(QrFormat.SMS, "SMSTO:010:hi", { content: QrContent.Sms -> content.copy(message = "") }, "SMSTO:010")
            assertPatch(QrFormat.EVENT, eventLocation, { content: QrContent.Event -> content.copy(location = "L") }, crlf("BEGIN:VEVENT", "SUMMARY:a", "RRULE:FREQ=WEEKLY", "LOCATION:L", "END:VEVENT"))
            assertPatch(QrFormat.EVENT, eventPeriod, { content: QrContent.Event -> content.copy(period = null) }, crlf("BEGIN:VEVENT", "SUMMARY:a", "END:VEVENT"))
        }

        test("TC-QR-ADD-DOMAIN-029 일정 기간을 고치면 종일 여부가 같을 때 시간대 표기를 그대로 둔다") {
            val raw = crlf("BEGIN:VEVENT", "SUMMARY:a", "DTSTART;TZID=Asia/Seoul:20260927T140000", "DTEND:20260927T060000Z", "END:VEVENT")
            val timed =
                QrEventPeriod.DateTime(
                    start = LocalDateTime(year = 2026, month = 9, day = 28, hour = 9, minute = 0),
                    endInclusive = LocalDateTime(year = 2026, month = 9, day = 28, hour = 10, minute = 0),
                )
            val allDay = QrEventPeriod.AllDay(start = LocalDate(2026, 9, 28), endInclusive = LocalDate(2026, 9, 28))

            assertPatch(
                QrFormat.EVENT,
                raw,
                { content: QrContent.Event -> content.copy(period = timed) },
                crlf("BEGIN:VEVENT", "SUMMARY:a", "DTSTART;TZID=Asia/Seoul:20260928T090000", "DTEND:20260928T100000Z", "END:VEVENT"),
            )
            assertPatch(
                QrFormat.EVENT,
                raw,
                { content: QrContent.Event -> content.copy(period = allDay) },
                crlf("BEGIN:VEVENT", "SUMMARY:a", "DTSTART;VALUE=DATE:20260928", "DTEND;VALUE=DATE:20260929", "END:VEVENT"),
            )
        }

        test("TC-QR-ADD-DOMAIN-030 위치의 좌표가 성립하지 않으면 입력한 글자를 그대로 담는다") {
            assertPatch(QrFormat.LOCATION, "geo:37.5,127", { content: QrContent.Location -> content.copy(latitude = " abc ") }, "geo:abc,127")
        }

        test("TC-QR-ADD-DOMAIN-031 새 줄을 넣을 때 QR 값에 쓰인 줄바꿈을 따른다") {
            assertPatch(
                QrFormat.CONTACT,
                lf("BEGIN:VCARD", "FN:a", "END:VCARD"),
                { content: QrContent.Contact -> content.copy(phoneNumber = "010") },
                lf("BEGIN:VCARD", "FN:a", "TEL:010", "END:VCARD"),
            )
        }

        test("그 포맷의 값이 아니면 입력을 읽지 않고 고치면 새로 만든 값을 준다") {
            QrFormat.WIFI.readContent("hello") shouldBe null
            QrContent.Wifi(ssid = "home").patch(raw = "hello", previous = QrContent.Wifi()) shouldBe "WIFI:T:WPA;S:home;;"
        }

        test("고친 입력이 없으면 QR 값을 그대로 둔다") {
            val raw = crlf("BEGIN:VCARD", "FN:a", "X-CUSTOM:1", "END:VCARD")
            val content = checkNotNull(QrFormat.CONTACT.readContent(raw))

            content.patch(raw = raw, previous = content) shouldBe raw
        }
    })

@Suppress("UNCHECKED_CAST")
private fun <T : QrContent> assertPatch(
    format: QrFormat,
    raw: String,
    edit: (T) -> T,
    expected: String,
) {
    val previous = checkNotNull(format.readContent(raw)) as T

    withClue("포맷=$format, QR 값=$raw") {
        edit(previous).patch(raw = raw, previous = previous) shouldBe expected
    }
}

private fun crlf(vararg line: String): String = line.joinToString(separator = "\r\n")

private fun lf(vararg line: String): String = line.joinToString(separator = "\n")
