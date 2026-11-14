package io.github.taetae98coding.diary.domain.share

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

class ShareTargetTest :
    FunSpec({
        val seoul = TimeZone.of("Asia/Seoul")

        fun ShareContent.target(): ShareTarget = toShareTarget(timeZone = seoul)

        test("TC-bf82a21c-315e-4e29-a8e4-80cb7c4391f2 글이 링크 하나로만 이루어졌을 때만 WebAdd를 연다") {
            val webList =
                listOf(
                    "https://example.com",
                    "http://example.com/a?b=1",
                    "  https://example.com\n",
                    "HTTPS://EXAMPLE.COM",
                    "https://www.youtube.com/watch?v=abc",
                    "https://naver.me/xyz",
                )
            val memoList = listOf("https://a.com https://b.com", "기사 제목\nhttps://a.com", "ftp://example.com", "example.com", "내일 10시 회의")

            webList.forEach { text -> withClue(text) { (ShareContent.Text(text = text).target() is ShareTarget.Web) shouldBe true } }
            memoList.forEach { text -> withClue(text) { (ShareContent.Text(text = text).target() is ShareTarget.Memo) shouldBe true } }
        }

        test("TC-b280acf1-29e3-44b8-8b9f-8960f3fa37c0 파일은 종류에 따라 여는 화면을 정한다") {
            (ShareContent.Contact(vCard = vCard("FN:김철수")).target() is ShareTarget.Contact) shouldBe true
            (ShareContent.Event(iCalendar = vEvent("SUMMARY:회의")).target() is ShareTarget.Memo) shouldBe true
        }

        test("TC-d5922752-2b00-4dd7-831c-0326e92737d4 공유 제목은 앞뒤 공백을 지워 쓰고 비어 있으면 없는 것으로 본다") {
            listOf("예시 페이지" to "예시 페이지", "  예시 페이지  " to "예시 페이지", "   " to "", "" to "").forEach { (subject, title) ->
                withClue(subject) {
                    ShareContent.Text(text = "https://example.com", subject = subject).target() shouldBe ShareTarget.Web(title = title, url = "https://example.com")
                }
            }
        }

        test("TC-2cf68c16-0bc4-4734-8ba2-f30a08dc8e22 WebAdd에는 앞뒤 공백을 지운 링크를 URL로 채운다") {
            ShareContent.Text(text = "  https://example.com  ").target() shouldBe ShareTarget.Web(title = "", url = "https://example.com")
        }

        test("TC-5ed13d91-b8ae-43c6-b683-403f2c62d50d 공유 제목이 있으면 메모 제목에 쓰고 글 전체를 설명에 채운다") {
            val subject = Arb.alphanumericString(prefix = "Subject").next()
            val text = listOf(Arb.alphanumericString(prefix = "First").next(), Arb.alphanumericString(prefix = "Second").next()).joinToString("\n")

            ShareContent.Text(text = text, subject = subject).target() shouldBe ShareTarget.Memo(title = subject, description = text)
        }

        test("TC-4b01d038-ecbe-4f09-a15e-ba4deb570e54 공유 제목이 없으면 첫 줄을 메모 제목, 나머지를 설명에 채운다") {
            val title = Arb.alphanumericString(prefix = "Title").next()
            val second = Arb.alphanumericString(prefix = "Second").next()
            val third = Arb.alphanumericString(prefix = "Third").next()

            listOf(
                title to ShareTarget.Memo(title = title, description = ""),
                listOf(title, second, third).joinToString("\n") to ShareTarget.Memo(title = title, description = listOf(second, third).joinToString("\n")),
                listOf("", "", "  $title  ", second, "").joinToString("\n") to ShareTarget.Memo(title = title, description = second),
                listOf(title, "", third).joinToString("\n") to ShareTarget.Memo(title = title, description = listOf("", third).joinToString("\n")),
            ).forEach { (text, expected) -> withClue(text) { ShareContent.Text(text = text).target() shouldBe expected } }
        }

        test("TC-11cb6554-1a1d-49c7-9ce1-9525a42269f6 일정의 제목, 설명과 장소를 메모에 채운다") {
            listOf(
                vEvent("SUMMARY:회의", "DESCRIPTION:안건 정리", "LOCATION:본사 3층") to ("회의" to "안건 정리\n장소: 본사 3층"),
                vEvent("SUMMARY:회의", "LOCATION:본사 3층") to ("회의" to "장소: 본사 3층"),
                vEvent("SUMMARY:회의", "DESCRIPTION:안건 정리") to ("회의" to "안건 정리"),
                vEvent("SUMMARY:회의\\, 주간", "DESCRIPTION:첫 줄\\n둘째 줄") to ("회의, 주간" to "첫 줄\n둘째 줄"),
            ).forEach { (raw, expected) ->
                withClue(raw) {
                    val memo = ShareContent.Event(iCalendar = raw).target() as ShareTarget.Memo
                    (memo.title to memo.description) shouldBe expected
                }
            }
        }

        test("TC-330a0380-5176-4e68-a6c5-b1893fbd896f 일정 안에 담긴 알림의 제목과 설명은 메모에 채우지 않는다") {
            val raw = vEvent("BEGIN:VALARM", "ACTION:DISPLAY", "SUMMARY:알림 제목", "DESCRIPTION:Reminder", "END:VALARM", "SUMMARY:회의")
            val memo = ShareContent.Event(iCalendar = raw).target() as ShareTarget.Memo

            memo.title shouldBe "회의"
            memo.description shouldBe ""
        }

        test("TC-13ccb301-53c3-4073-ba2e-618b24fc1b3b quoted-printable로 적힌 값은 풀어서 채운다") {
            listOf(
                vCard("FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=ED=99=8D=EA=B8=B8=EB=8F=99") to "홍길동",
                vCard("N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=ED=99=8D;=EA=B8=B8=EB=8F=99;;;") to "홍 길동",
                vCard("FN;QUOTED-PRINTABLE:=ED=99=8D=", "=EA=B8=B8=EB=8F=99") to "홍길동",
                vCard("FN:=ED=99=8D") to "=ED=99=8D",
            ).forEach { (raw, expected) ->
                withClue(raw) { (ShareContent.Contact(vCard = raw).target() as ShareTarget.Contact).name shouldBe expected }
            }

            val memo = ShareContent.Event(iCalendar = vEvent("SUMMARY;ENCODING=QUOTED-PRINTABLE:=ED=9A=8C=EC=9D=98")).target() as ShareTarget.Memo
            memo.title shouldBe "회의"
        }

        test("quoted-printable 값이 줄 끝의 =로 끝난 채 연락처가 끝나도 읽은 데까지 채운다") {
            val contact = ShareContent.Contact(vCard = vCard("FN;ENCODING=QUOTED-PRINTABLE:=ED=99=8D=")).target() as ShareTarget.Contact

            contact.name shouldBe "홍"
        }

        test("TC-e6632fe9-6aa4-407e-9407-017d47ba4e4c 일정의 시작과 종료로 메모 기간을 채운다") {
            listOf(
                listOf("DTSTART;VALUE=DATE:20261120", "DTEND;VALUE=DATE:20261122") to SharePeriod.AllDay(LocalDate(2026, 11, 20), LocalDate(2026, 11, 21)),
                listOf("DTSTART;VALUE=DATE:20261120") to SharePeriod.AllDay(LocalDate(2026, 11, 20), LocalDate(2026, 11, 20)),
                listOf("DTSTART:20261120", "DTEND:20261122") to SharePeriod.AllDay(LocalDate(2026, 11, 20), LocalDate(2026, 11, 21)),
                listOf("DTSTART:20261120T010000Z", "DTEND:20261120T023000Z") to SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 10, 0), LocalDateTime(2026, 11, 20, 11, 30)),
                listOf("DTSTART;TZID=America/New_York:20261120T090000", "DTEND;TZID=America/New_York:20261120T100000") to
                    SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 23, 0), LocalDateTime(2026, 11, 21, 0, 0)),
                listOf("DTSTART:20261120T090000", "DTEND:20261120T100000") to SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 9, 0), LocalDateTime(2026, 11, 20, 10, 0)),
                listOf("DTSTART:20261120T090000") to SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 9, 0), LocalDateTime(2026, 11, 20, 9, 0)),
                listOf("DTSTART:20261120T090000", "DTEND:20261120T080000") to SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 9, 0), LocalDateTime(2026, 11, 20, 9, 0)),
                listOf("DTSTART;TZID=Korea Standard Time:20261120T090000", "DTEND;TZID=Korea Standard Time:20261120T100000") to
                    SharePeriod.DateTime(LocalDateTime(2026, 11, 20, 9, 0), LocalDateTime(2026, 11, 20, 10, 0)),
                listOf("DTEND:20261120T100000") to null,
            ).forEach { (lineList, expected) ->
                withClue(lineList) {
                    val memo = ShareContent.Event(iCalendar = vEvent("SUMMARY:회의", *lineList.toTypedArray())).target() as ShareTarget.Memo
                    memo.period shouldBe expected
                }
            }
        }

        test("TC-26a020ce-ec0f-45d3-88fc-84d13218c1a9 일정이 여러 개면 첫 일정만 쓴다") {
            val raw = vEvent("SUMMARY:첫째", "DESCRIPTION:첫 설명", "DTSTART;VALUE=DATE:20261120") + "\r\n" + vEvent("SUMMARY:둘째", "DESCRIPTION:둘째 설명", "DTSTART;VALUE=DATE:20261201")

            ShareContent.Event(iCalendar = raw).target() shouldBe
                ShareTarget.Memo(title = "첫째", description = "첫 설명", period = SharePeriod.AllDay(LocalDate(2026, 11, 20), LocalDate(2026, 11, 20)))
        }

        test("TC-d75bdb3a-1dad-4570-80aa-122cfbb25cbf 연락처의 이름과 전화번호를 채운다") {
            listOf(
                vCard("FN:김철수", "TEL:010-1111-2222", "TEL:02-333-4444") to ("김철수" to listOf("010-1111-2222", "02-333-4444")),
                vCard("N:김;철수;;;") to ("김 철수" to emptyList()),
                vCard("TEL;TYPE=CELL:010-1111-2222") to ("" to listOf("010-1111-2222")),
                vCard("FN:김철수", "TEL:   ", "TEL:010-1111-2222") to ("김철수" to listOf("010-1111-2222")),
            ).forEach { (raw, expected) ->
                withClue(raw) {
                    val contact = ShareContent.Contact(vCard = raw).target() as ShareTarget.Contact
                    (contact.name to contact.phoneNumberList) shouldBe expected
                }
            }
        }

        test("TC-58c71280-9037-4354-bbed-15a04e14fdd9 연도가 있는 생일은 양력 생일로 채운다") {
            val contact = ShareContent.Contact(vCard = vCard("FN:김철수", "BDAY:1990-03-15")).target() as ShareTarget.Contact

            contact.birthday shouldBe LocalDate(1990, 3, 15)
            contact.description shouldBe ""
        }

        test("TC-e06a0262-07b4-4eae-b84e-595e758ab29b ContactAdd에 입력이 없는 값은 정해진 순서로 설명에 채운다") {
            val raw =
                vCard(
                    "FN:김철수",
                    "BDAY:--0315",
                    "URL:https://a.com",
                    "ADR:;;강남대로 1;서울;;;",
                    "ORG:다이어리;개발팀",
                    "EMAIL:a@b.com",
                    "EMAIL:c@d.com",
                    "NOTE:대학 동기",
                )
            val contact = ShareContent.Contact(vCard = raw).target() as ShareTarget.Contact

            contact.birthday shouldBe null
            contact.description shouldBe
                listOf("대학 동기", "이메일: a@b.com", "이메일: c@d.com", "회사: 다이어리", "주소: 강남대로 1 서울", "웹사이트: https://a.com", "생일: 3월 15일").joinToString("\n")
        }

        test("TC-914870fd-646b-4b3f-ac49-46ba563655e8 날짜로 읽을 수 없는 생일은 적힌 값을 설명에 채운다") {
            val contact = ShareContent.Contact(vCard = vCard("FN:김철수", "EMAIL:a@b.com", "BDAY:생일모름")).target() as ShareTarget.Contact

            contact.birthday shouldBe null
            contact.description shouldBe "이메일: a@b.com\n생일: 생일모름"
        }

        test("TC-004e53eb-2489-41ac-a8e0-0becf153a031 연락처가 여러 개면 첫 연락처만 쓴다") {
            val raw = vCard("FN:첫째", "TEL:010-1", "NOTE:첫 메모") + "\r\n" + vCard("FN:둘째", "TEL:010-2", "NOTE:둘째 메모")

            ShareContent.Contact(vCard = raw).target() shouldBe
                ShareTarget.Contact(name = "첫째", description = "첫 메모", phoneNumberList = listOf("010-1"), birthday = null)
        }
    })

private fun vCard(vararg lineList: String): String = (listOf("BEGIN:VCARD", "VERSION:3.0") + lineList + "END:VCARD").joinToString("\r\n")

private fun vEvent(vararg lineList: String): String = (listOf("BEGIN:VCALENDAR", "BEGIN:VEVENT") + lineList + listOf("END:VEVENT", "END:VCALENDAR")).joinToString("\r\n")
