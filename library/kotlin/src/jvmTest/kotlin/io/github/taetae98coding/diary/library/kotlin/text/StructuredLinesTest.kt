package io.github.taetae98coding.diary.library.kotlin.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

class StructuredLinesTest :
    FunSpec({
        fun lines(vararg lineList: String): StructuredLines = StructuredLines.parse(lineList.joinToString("\r\n"))

        fun property(line: String): StructuredLines.Property = checkNotNull(lines(line).entries.single().property)

        test("묶음 자체의 항목만 주고 안에서 다시 시작하는 묶음의 항목은 뺀다") {
            val entryList =
                lines("BEGIN:VCALENDAR", "BEGIN:VEVENT", "SUMMARY:a", "BEGIN:VALARM", "DESCRIPTION:b", "END:VALARM", "LOCATION:c", "END:VEVENT", "BEGIN:VEVENT", "SUMMARY:d", "END:VEVENT")
                    .blockEntries("VEVENT")

            entryList.map(StructuredLines.Entry::text) shouldBe listOf("SUMMARY:a", "LOCATION:c")
        }

        test("묶음이 없으면 항목이 없고 끝이 없으면 마지막 줄까지 준다") {
            lines("BEGIN:VCARD", "FN:a", "END:VCARD").blockEntries("VEVENT") shouldBe emptyList()
            lines("begin:vevent", "SUMMARY:a").blockEntries("VEVENT").map(StructuredLines.Entry::text) shouldBe listOf("SUMMARY:a")
        }

        test("VALUE=DATE이거나 여덟 자리뿐인 값은 날짜로 읽는다") {
            property("DTSTART;VALUE=DATE:20260927").toICalendarTime() shouldBe ICalendarTime.Date(date = LocalDate(2026, 9, 27))
            property("DTSTART:20260927").toICalendarTime() shouldBe ICalendarTime.Date(date = LocalDate(2026, 9, 27))
            property("DTSTART:20261327").toICalendarTime() shouldBe null
        }

        test("시각이 있는 값은 적힌 숫자와 적힌 시간대로 읽는다") {
            val dateTime = LocalDateTime(year = 2026, month = 9, day = 27, hour = 14, minute = 30)

            property("DTSTART:20260927T143000").toICalendarTime() shouldBe ICalendarTime.DateTime(dateTime = dateTime, timeZone = null)
            property("DTSTART:20260927T143000Z").toICalendarTime() shouldBe ICalendarTime.DateTime(dateTime = dateTime, timeZone = TimeZone.UTC)
            property("DTSTART;TZID=Asia/Seoul:20260927T143000").toICalendarTime() shouldBe ICalendarTime.DateTime(dateTime = dateTime, timeZone = TimeZone.of("Asia/Seoul"))
            property("DTSTART;TZID=Korea Standard Time:20260927T143000").toICalendarTime() shouldBe ICalendarTime.DateTime(dateTime = dateTime, timeZone = null)
            property("DTSTART:내일").toICalendarTime() shouldBe null
        }

        test(";로 나뉜 값은 비어 있지 않은 부분을 공백으로 잇고 첫 부분만 꺼낼 수 있다") {
            ";;서울;강남\\;구;;".joinStructuredComponents() shouldBe "서울 강남;구"
            "회사;부서".firstStructuredComponent() shouldBe "회사"
            "".firstStructuredComponent() shouldBe ""
        }
    })
