package io.github.taetae98coding.diary.domain.qr.content

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class QrContentTest :
    FunSpec({
        test("TC-QR-ADD-DOMAIN-018 필수 입력이 성립하면 포맷별 규칙대로 QR 값을 만든다") {
            val caseList =
                listOf(
                    QrContent.Url(url = "https://a.com") to "https://a.com",
                    QrContent.Contact(name = "홍길동") to lines("BEGIN:VCARD", "VERSION:3.0", "N:홍길동", "FN:홍길동", "END:VCARD"),
                    QrContent.Contact(
                        name = "홍길동",
                        phoneNumber = "010-1234",
                        email = "a@b.c",
                        company = "회사",
                        address = "서울",
                        website = "https://a.com",
                    ) to
                        lines(
                            "BEGIN:VCARD",
                            "VERSION:3.0",
                            "N:홍길동",
                            "FN:홍길동",
                            "ORG:회사",
                            "TEL:010-1234",
                            "EMAIL:a@b.c",
                            "ADR:;;서울;;;;",
                            "URL:https://a.com",
                            "END:VCARD",
                        ),
                    QrContent.Wifi(ssid = "home", security = QrWifiSecurity.WPA, password = "pw") to "WIFI:T:WPA;S:home;P:pw;;",
                    QrContent.Wifi(ssid = "home", security = QrWifiSecurity.WEP, password = "pw", isHidden = true) to "WIFI:T:WEP;S:home;P:pw;H:true;;",
                    QrContent.Wifi(ssid = "home", security = QrWifiSecurity.WPA) to "WIFI:T:WPA;S:home;;",
                    QrContent.Wifi(ssid = "home", security = QrWifiSecurity.NONE, password = "pw") to "WIFI:T:nopass;S:home;;",
                    QrContent.Location(latitude = "37.5665", longitude = "126.9780") to "geo:37.5665,126.978",
                    QrContent.Email(to = "a@b.c") to "mailto:a@b.c",
                    QrContent.Email(to = "a@b.c", body = "hi") to "mailto:a@b.c?body=hi",
                    QrContent.Email(to = "a@b.c", subject = "hi", body = "yo") to "mailto:a@b.c?subject=hi&body=yo",
                    QrContent.Phone(phoneNumber = "010-1234") to "tel:010-1234",
                    QrContent.Sms(phoneNumber = "010", message = "hi") to "SMSTO:010:hi",
                    QrContent.Sms(phoneNumber = "010") to "SMSTO:010",
                    QrContent.Event(
                        title = "회의",
                        period =
                            QrEventPeriod.DateTime(
                                start = LocalDateTime(year = 2026, month = 9, day = 27, hour = 14, minute = 0),
                                endInclusive = LocalDateTime(year = 2026, month = 9, day = 27, hour = 15, minute = 30),
                            ),
                    ) to lines("BEGIN:VEVENT", "SUMMARY:회의", "DTSTART:20260927T140000", "DTEND:20260927T153000", "END:VEVENT"),
                    QrContent.Event(
                        title = "여행",
                        period = QrEventPeriod.AllDay(start = LocalDate(2026, 9, 27), endInclusive = LocalDate(2026, 9, 28)),
                        location = "부산",
                        description = "짐",
                    ) to
                        lines(
                            "BEGIN:VEVENT",
                            "SUMMARY:여행",
                            "DTSTART;VALUE=DATE:20260927",
                            "DTEND;VALUE=DATE:20260929",
                            "LOCATION:부산",
                            "DESCRIPTION:짐",
                            "END:VEVENT",
                        ),
                )

            caseList.forEach { (content, expected) ->
                withClue("입력=$content") {
                    content.encode() shouldBe expected
                }
            }
        }

        test("TC-QR-ADD-DOMAIN-019 포맷 규칙이 정한 글자는 바꿔 담는다") {
            val caseList =
                listOf(
                    QrContent.Contact(name = "a,b;c\\d", address = "1층\n2호") to
                        lines("BEGIN:VCARD", "VERSION:3.0", """N:a\,b\;c\\d""", """FN:a\,b\;c\\d""", """ADR:;;1층\n2호;;;;""", "END:VCARD"),
                    QrContent.Wifi(ssid = """a;b,c:d"e\f""", security = QrWifiSecurity.WPA, password = "p;w") to
                        """WIFI:T:WPA;S:a\;b\,c\:d\"e\\f;P:p\;w;;""",
                    QrContent.Email(to = "a@b.c", subject = "안녕 a&b", body = "x\ny") to
                        "mailto:a@b.c?subject=%EC%95%88%EB%85%95%20a%26b&body=x%0D%0Ay",
                    QrContent.Event(
                        title = "a,b;c\\d",
                        period = QrEventPeriod.AllDay(start = LocalDate(2026, 9, 27), endInclusive = LocalDate(2026, 9, 27)),
                        description = "x\ny",
                    ) to
                        lines(
                            "BEGIN:VEVENT",
                            """SUMMARY:a\,b\;c\\d""",
                            "DTSTART;VALUE=DATE:20260927",
                            "DTEND;VALUE=DATE:20260928",
                            """DESCRIPTION:x\ny""",
                            "END:VEVENT",
                        ),
                )

            caseList.forEach { (content, expected) ->
                withClue("입력=$content") {
                    content.encode() shouldBe expected
                }
            }
        }

        test("TC-QR-ADD-DOMAIN-020 텍스트와 Wi-Fi 외의 입력은 앞뒤 공백을 지워 담고 Wi-Fi의 이름과 비밀번호는 그대로 담는다") {
            val caseList =
                listOf(
                    QrContent.Url(url = "  https://a.com  ") to "https://a.com",
                    QrContent.Phone(phoneNumber = " 010 1234 ") to "tel:010 1234",
                    QrContent.Sms(phoneNumber = "010", message = "   ") to "SMSTO:010",
                    QrContent.Wifi(ssid = " home ", security = QrWifiSecurity.WPA, password = " pw ") to "WIFI:T:WPA;S: home ;P: pw ;;",
                )

            caseList.forEach { (content, expected) ->
                withClue("입력=$content") {
                    content.encode() shouldBe expected
                }
            }
        }

        test("TC-QR-ADD-DOMAIN-022 위치 좌표는 소수 여섯째 자리로 반올림하고 끝의 0을 빼고 담는다") {
            val caseList =
                listOf(
                    QrContent.Location(latitude = "37.12345678", longitude = "127.1234564") to "geo:37.123457,127.123456",
                    QrContent.Location(latitude = "37.500000", longitude = "127.0") to "geo:37.5,127",
                    QrContent.Location(latitude = "-0.0000001", longitude = " +126.5 ") to "geo:0,126.5",
                    QrContent.Location(latitude = "90", longitude = "-180") to "geo:90,-180",
                )

            caseList.forEach { (content, expected) ->
                withClue("입력=$content") {
                    content.encode() shouldBe expected
                }
            }
        }

        test("공백만 있는 텍스트도 QR 값이 있다") {
            QrContent.Text(text = " ").encode() shouldBe " "
        }

        test("비어 있는 입력이 있어도 지금 입력으로 값을 만든다") {
            QrContent.Contact(address = "서울").encode() shouldBe lines("BEGIN:VCARD", "VERSION:3.0", "N:", "FN:", "ADR:;;서울;;;;", "END:VCARD")
            QrContent.Event(title = "회의").encode() shouldBe lines("BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT")
        }

        test("유효한 위치만 좌표를 가진다") {
            QrContent.Location(latitude = "37.5", longitude = "127").coordinate.shouldNotBeNull()
            QrContent.Location(latitude = "37.5", longitude = "180.1").coordinate shouldBe null
        }
    })

private fun lines(vararg line: String): String = line.joinToString(separator = "\r\n")
