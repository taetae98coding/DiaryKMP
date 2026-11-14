package io.github.taetae98coding.diary.domain.memo.share

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.share.ShareContent
import io.github.taetae98coding.diary.domain.share.SharePeriod
import io.github.taetae98coding.diary.domain.share.ShareTarget
import io.github.taetae98coding.diary.domain.share.toShareTarget
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoICalendarTest :
    FunSpec({
        test("TC-61def525-e4e5-4729-a5be-66b7beaa8e80 일정 하나를 담은 iCalendar 틀로 만든다") {
            val id = Arb.kotlinUuid().next()
            val title = Arb.alphanumericString(prefix = "Title").next()
            val sharedAt = Arb.secondPrecisionInstant().next()

            val lineList = calendarOf(id = id, title = title, sharedAt = sharedAt).unfoldedLineList()

            lineList.take(4) shouldBe listOf("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//Diary//Diary//EN", "BEGIN:VEVENT")
            lineList.takeLast(2) shouldBe listOf("END:VEVENT", "END:VCALENDAR")
            lineList.drop(4).take(3) shouldBe listOf("UID:$id@diary", "DTSTAMP:${sharedAt.toUtcText()}", "SUMMARY:$title")
            lineList.count { line -> line == "BEGIN:VEVENT" } shouldBe 1
        }

        test("TC-d1e4034f-f3cc-42a8-819e-133a45d6e6d6 종일 기간은 날짜로 담고 종료에는 종료일의 다음 날을 담는다") {
            listOf(
                Triple(LocalDateRange(LocalDate(2026, 11, 20), LocalDate(2026, 11, 20)), "DTSTART;VALUE=DATE:20261120", "DTEND;VALUE=DATE:20261121"),
                Triple(LocalDateRange(LocalDate(2026, 12, 30), LocalDate(2026, 12, 31)), "DTSTART;VALUE=DATE:20261230", "DTEND;VALUE=DATE:20270101"),
            ).forEach { (dateRange, startLine, endLine) ->
                val lineList = calendarOf(dateTime = MemoDateTime.AllDay(dateRange = dateRange)).unfoldedLineList()

                lineList shouldContainInOrder listOf(startLine, endLine)
            }
        }

        test("TC-015f2b4f-6644-4abd-a32f-39abf83a6c07 날짜·시간 기간은 기기 시간대의 시각을 세계 협정시로 바꿔 담는다") {
            listOf(
                DateTimeCase(SEOUL, LocalDateTime(2026, 11, 20, 19, 0), LocalDateTime(2026, 11, 20, 21, 0), "DTSTART:20261120T100000Z", "DTEND:20261120T120000Z"),
                DateTimeCase(SEOUL, LocalDateTime(2026, 11, 21, 1, 0), LocalDateTime(2026, 11, 21, 2, 30), "DTSTART:20261120T160000Z", "DTEND:20261120T173000Z"),
                DateTimeCase(TimeZone.UTC, LocalDateTime(2026, 11, 20, 19, 0), LocalDateTime(2026, 11, 20, 21, 0), "DTSTART:20261120T190000Z", "DTEND:20261120T210000Z"),
            ).forEach { case ->
                val lineList =
                    calendarOf(dateTime = MemoDateTime.DateTime(start = case.start, endInclusive = case.end), timeZone = case.timeZone)
                        .unfoldedLineList()

                lineList shouldContainInOrder listOf(case.startLine, case.endLine)
            }
        }

        test("TC-f448ea48-6cec-4a73-aecc-1d1a6f25c2ae 설명이 없거나 공백뿐이면 설명 줄을 담지 않는다") {
            listOf("", " \n\t ").forEach { description ->
                val lineList = calendarOf(description = description).unfoldedLineList()

                lineList.none { line -> line.startsWith("DESCRIPTION") } shouldBe true
            }
        }

        test("TC-6f362645-34b6-40e2-bc0c-5f3f673a0519 제목과 설명은 앞뒤 공백을 지우고 특수 글자를 바꿔 담는다") {
            listOf(
                """a\b,c;d""" to """a\\b\,c\;d""",
                "첫째\n둘째" to "첫째\\n둘째",
                "첫째\r\n둘째" to "첫째\\n둘째",
                "첫째\r둘째" to "첫째\\n둘째",
            ).forEach { (value, escaped) ->
                val lineList = calendarOf(title = "  $value\n", description = "\t$value  ").unfoldedLineList()

                lineList shouldContainInOrder listOf("SUMMARY:$escaped", "DESCRIPTION:$escaped")
            }
        }

        test("TC-978ae235-23ce-49bd-84f8-1dfb18332b7d 줄 사이는 CR LF로 나눈다") {
            val calendar = calendarOf(title = "제목\n줄바꿈", description = "설명\r\n줄바꿈\r끝")

            calendar.replace("\r\n", "") shouldNotContain "\r"
            calendar.replace("\r\n", "") shouldNotContain "\n"
            calendar.split("\r\n").last() shouldBe ""
        }

        test("TC-848f44f1-62a8-4612-91d9-d15d95d11782 75바이트를 넘는 줄은 글자를 깨뜨리지 않고 나눈다") {
            listOf("a", "가", "😀").forEach { unit ->
                val description = unit.repeat(100)

                val calendar = calendarOf(description = description)
                val physicalLineList = calendar.split("\r\n").dropLast(1)

                physicalLineList.forEach { line -> line.encodeToByteArray().size shouldBeLessThanOrEqual 75 }
                physicalLineList.forEach { line -> line.encodeToByteArray().decodeToString() shouldBe line }
                physicalLineList.forEach { line -> line shouldNotContain "�" }
                physicalLineList.filter { line -> line.startsWith(" ") }.size shouldBeGreaterThan 0
                calendar.unfoldedLineList() shouldContainInOrder listOf("DESCRIPTION:$description")
            }
        }

        test("TC-4bfe46f6-6851-4916-98bb-4c235886bcae 공유로 받으면 같은 제목, 설명, 기간이 채워진다") {
            val dateRange = Arb.localDateRange().next()
            listOf(
                RoundTripCase(
                    title = "회식, 장소; 미정 \\ " + "가".repeat(40),
                    description = "첫 줄\n둘째 줄, 세미콜론; 역슬래시 \\ " + "나".repeat(40),
                    dateTime = MemoDateTime.AllDay(dateRange = dateRange),
                    timeZone = SEOUL,
                ),
                RoundTripCase(
                    title = Arb.alphanumericString(prefix = "Title").next(),
                    description = Arb.alphanumericString(prefix = "Description").next(),
                    dateTime = MemoDateTime.DateTime(start = LocalDateTime(2026, 11, 20, 23, 0), endInclusive = LocalDateTime(2026, 11, 21, 1, 30)),
                    timeZone = SEOUL,
                ),
            ).forEach { case ->
                val calendar = calendarOf(title = case.title, description = case.description, dateTime = case.dateTime, timeZone = case.timeZone)

                val target = ShareContent.Event(iCalendar = calendar).toShareTarget(timeZone = case.timeZone)

                target shouldBe ShareTarget.Memo(title = case.title, description = case.description, period = case.dateTime.toSharePeriod())
            }
        }

        test("공유한 시각은 그 순간을 세계 협정시로 담는다") {
            val sharedAt = Instant.parse("2026-11-20T10:15:30Z")

            calendarOf(sharedAt = sharedAt).unfoldedLineList() shouldContainInOrder listOf("DTSTAMP:20261120T101530Z")
        }
    })

private val SEOUL: TimeZone = TimeZone.of("Asia/Seoul")

private data class DateTimeCase(
    val timeZone: TimeZone,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val startLine: String,
    val endLine: String,
)

private data class RoundTripCase(
    val title: String,
    val description: String,
    val dateTime: MemoDateTime,
    val timeZone: TimeZone,
)

private fun calendarOf(
    id: Uuid = Arb.kotlinUuid().next(),
    title: String = Arb.alphanumericString(prefix = "Title").next(),
    description: String = Arb.alphanumericString(prefix = "Description").next(),
    dateTime: MemoDateTime = MemoDateTime.AllDay(dateRange = Arb.localDateRange().next()),
    timeZone: TimeZone = SEOUL,
    sharedAt: Instant = Arb.secondPrecisionInstant().next(),
): String = memoICalendarOf(id = id, title = title, description = description, dateTime = dateTime, timeZone = timeZone, sharedAt = sharedAt)

private fun String.unfoldedLineList(): List<String> = replace("\r\n ", "").split("\r\n").dropLast(1)

private fun Instant.toUtcText(): String {
    val dateTime = toLocalDateTime(TimeZone.UTC)
    return "%04d%02d%02dT%02d%02d%02dZ".format(dateTime.year, dateTime.month.number, dateTime.day, dateTime.hour, dateTime.minute, dateTime.second)
}

private fun MemoDateTime.toSharePeriod(): SharePeriod =
    when (this) {
        is MemoDateTime.AllDay -> SharePeriod.AllDay(start = dateRange.start, endInclusive = dateRange.endInclusive)
        is MemoDateTime.DateTime -> SharePeriod.DateTime(start = start, endInclusive = endInclusive)
    }
