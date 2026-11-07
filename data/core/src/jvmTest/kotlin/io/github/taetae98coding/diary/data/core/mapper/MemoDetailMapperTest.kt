package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.testing.memo.memoDateTimeAllDay
import io.github.taetae98coding.diary.core.testing.memo.memoDateTimeDateTime
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.core.testing.memo.memoDetailLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class MemoDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = Arb.memoDetail().next()
            val allDay = Arb.memoDateTimeAllDay().next()
            val dateTime = Arb.memoDateTimeDateTime().next()

            listOf(
                domain.copy(dateTime = allDay) to
                    domain.expectedLocal(
                        isAllDay = true,
                        start = LocalDateTime(date = allDay.dateRange.start, time = Midnight),
                        endInclusive = LocalDateTime(date = allDay.dateRange.endInclusive, time = Midnight),
                    ),
                domain.copy(dateTime = dateTime) to
                    domain.expectedLocal(
                        isAllDay = false,
                        start = dateTime.start,
                        endInclusive = dateTime.endInclusive,
                    ),
                domain.copy(dateTime = null) to
                    domain.expectedLocal(
                        isAllDay = null,
                        start = null,
                        endInclusive = null,
                    ),
            ).forEach { (case, expected) ->
                case.toLocal() shouldBe expected
            }
        }

        test("local to domain") {
            val start = Arb.kotlinxLocalDateTime().next()
            val endInclusive = Arb.kotlinxLocalDateTime().next()
            val local =
                Arb
                    .memoDetailLocalEntity()
                    .next()
                    .copy(start = start, endInclusive = endInclusive)

            listOf(
                local.copy(isAllDay = true) to
                    MemoDateTime.AllDay(dateRange = start.date..endInclusive.date),
                local.copy(isAllDay = false) to
                    MemoDateTime.DateTime(start = start, endInclusive = endInclusive),
                local.copy(isAllDay = null) to null,
                local.copy(isAllDay = true, start = null) to null,
                local.copy(isAllDay = true, endInclusive = null) to null,
                local.copy(isAllDay = null, start = null, endInclusive = null) to null,
            ).forEach { (case, expected) ->
                case.toDomain() shouldBe case.expectedDomain(dateTime = expected)
            }
        }

        test("domain to local to domain") {
            val domain = Arb.memoDetail().next()

            listOf(
                Arb.memoDateTimeAllDay().next(),
                Arb.memoDateTimeDateTime().next(),
                null,
            ).forEach { dateTime ->
                val expected = domain.copy(dateTime = dateTime)

                expected.toLocal().toDomain() shouldBe expected
            }
        }
    })

private val Midnight = LocalTime(hour = 0, minute = 0)

private fun MemoDetail.expectedLocal(
    isAllDay: Boolean?,
    start: LocalDateTime?,
    endInclusive: LocalDateTime?,
): MemoDetailLocalEntity =
    MemoDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = isAllDay,
        start = start,
        endInclusive = endInclusive,
    )

private fun MemoDetailLocalEntity.expectedDomain(dateTime: MemoDateTime?): MemoDetail =
    MemoDetail(
        title = title,
        description = description,
        color = color,
        dateTime = dateTime,
    )
