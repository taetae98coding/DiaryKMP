package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.CalendarDDayOccurrence
import io.github.taetae98coding.diary.core.database.impl.dday.baseDay
import io.github.taetae98coding.diary.core.database.impl.dday.days
import io.github.taetae98coding.diary.core.database.impl.dday.months
import io.github.taetae98coding.diary.core.database.impl.dday.toOccurrence
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.years
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.uuid.Uuid

// 디데이 테스트 케이스의 기념일 계산을 캘린더 디데이 조회 결과로 확인한다. 기념일의 날짜와 문구는 이 조회가 정한다.
class AccountCalendarDDayLocalDataSourceImplAnniversaryTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountCalendarDDayLocalDataSourceImpl
        lateinit var transaction: AccountDDayTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountCalendarDDayLocalDataSourceImpl(database = database)
            transaction = AccountDDayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insert(
            accountId: Uuid,
            dday: DDayLocalEntity,
        ) {
            transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
        }

        suspend fun occurrenceList(
            accountId: Uuid,
            start: LocalDate,
            endInclusive: LocalDate,
        ): List<CalendarDDayOccurrence> =
            dataSource
                .get(accountId = accountId, dateRange = start..endInclusive)
                .first()
                .map { calendarDDay -> calendarDDay.toOccurrence() }

        // 기준일 당일을 빼고 기념일만 본다.
        suspend fun anniversaryList(
            dday: DDayLocalEntity,
            endInclusive: LocalDate,
            start: LocalDate = dday.detail.baseDate,
        ): List<CalendarDDayOccurrence> {
            val accountId = Arb.kotlinUuid().next()
            insert(accountId = accountId, dday = dday)

            return occurrenceList(accountId = accountId, start = start, endInclusive = endInclusive)
                .filterNot { occurrence -> occurrence.kind == CalendarDDayKindLocalEntity.BASE_DAY }
        }

        test("TC-3d3eb2d5-7cd9-47b7-949f-7279ad5f26b1 일 간격의 기념일은 기준일을 1일로 세기에 따라 하루 달라진다") {
            val baseDate = LocalDate(2026, 1, 1)
            listOf(
                Row(interval = 100, countsBaseDay = true, expected = days(LocalDate(2026, 4, 10), 100)),
                Row(interval = 100, countsBaseDay = false, expected = days(LocalDate(2026, 4, 11), 100)),
                Row(interval = 100, countsBaseDay = true, expected = days(LocalDate(2026, 10, 27), 300)),
                Row(interval = 100, countsBaseDay = false, expected = days(LocalDate(2026, 10, 28), 300)),
                Row(interval = 1, countsBaseDay = false, expected = days(LocalDate(2026, 1, 2), 1)),
                Row(interval = 1, countsBaseDay = true, expected = days(LocalDate(2026, 1, 2), 2)),
            ).forEach { row ->
                val dday =
                    Arb
                        .localDDay(
                            baseDate = baseDate,
                            countsBaseDay = row.countsBaseDay,
                            repeats = setOf(DateTimeUnit.DayBased(days = row.interval)),
                        ).next()

                anniversaryList(dday = dday, start = row.expected.date, endInclusive = row.expected.date) shouldBe listOf(row.expected)
            }
        }

        test("TC-58ef2c8f-f2c1-4baa-a415-c7d7a4ba8d07 개월 간격의 기념일은 기준일과 같은 일에 돌아온다") {
            val baseDate = LocalDate(2026, 1, 15)
            val endInclusive = LocalDate(2028, 12, 31)
            listOf(true, false).forEach { countsBaseDay ->
                val monthly = Arb.localDDay(baseDate = baseDate, countsBaseDay = countsBaseDay, repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next()
                val yearly = Arb.localDDay(baseDate = baseDate, countsBaseDay = countsBaseDay, repeats = setOf(DateTimeUnit.MonthBased(months = 12))).next()

                anniversaryList(dday = monthly, endInclusive = endInclusive) shouldBe
                    (1..35).map { month -> months(baseDate.plus(month, DateTimeUnit.MONTH), month) }
                anniversaryList(dday = yearly, endInclusive = endInclusive) shouldBe
                    listOf(years(LocalDate(2027, 1, 15), 1), years(LocalDate(2028, 1, 15), 2))
            }
        }

        test("TC-71ba8ea0-d83f-44ef-b581-535554895c13 기준일보다 앞에는 기념일이 없다") {
            val dday =
                Arb
                    .localDDay(
                        baseDate = LocalDate(2026, 1, 15),
                        repeats = setOf(DateTimeUnit.DayBased(days = 1), DateTimeUnit.MonthBased(months = 1), DateTimeUnit.MonthBased(months = 12)),
                    ).next()

            anniversaryList(dday = dday, start = LocalDate(2025, 1, 1), endInclusive = LocalDate(2026, 1, 14)).shouldBeEmpty()
        }

        test("TC-8eaf6ca1-9613-4324-89fa-ac9fef327242 기념일 문구는 간격의 단위로 기준일에서 지난 만큼을 쓴다") {
            val baseDate = LocalDate(2026, 1, 15)
            listOf(
                Triple(DateTimeUnit.DayBased(days = 100), 3, CalendarDDayKindLocalEntity.DAYS to 300),
                Triple(DateTimeUnit.MonthBased(months = 3), 2, CalendarDDayKindLocalEntity.MONTHS to 6),
                Triple(DateTimeUnit.MonthBased(months = 6), 2, CalendarDDayKindLocalEntity.MONTHS to 12),
                Triple(DateTimeUnit.MonthBased(months = 18), 1, CalendarDDayKindLocalEntity.MONTHS to 18),
                Triple(DateTimeUnit.MonthBased(months = 12), 2, CalendarDDayKindLocalEntity.YEARS to 2),
                Triple(DateTimeUnit.MonthBased(months = 24), 1, CalendarDDayKindLocalEntity.YEARS to 2),
                Triple(DateTimeUnit.MonthBased(months = 36), 2, CalendarDDayKindLocalEntity.YEARS to 6),
            ).forEach { (interval, order, label) ->
                val dday = Arb.localDDay(baseDate = baseDate, repeats = setOf(interval)).next()

                val anniversary = anniversaryList(dday = dday, endInclusive = LocalDate(2033, 12, 31))[order - 1]

                (anniversary.kind to anniversary.amount) shouldBe label
            }
        }

        test("TC-a39bfc0d-7938-4d72-acdc-a50a4cda5341 달력에 없는 날짜에 걸린 개월 간격 기념일은 처리 방식에 따른다") {
            listOf(
                OverflowRow(LocalDate(2026, 1, 31), 1, 1, DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END, months(LocalDate(2026, 2, 28), 1)),
                OverflowRow(LocalDate(2026, 1, 31), 1, 1, DDayOverflowPolicyLocalEntity.SKIP, null),
                OverflowRow(LocalDate(2026, 1, 31), 1, 1, DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY, months(LocalDate(2026, 3, 1), 1)),
                OverflowRow(LocalDate(2028, 1, 31), 1, 1, DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END, months(LocalDate(2028, 2, 29), 1)),
                OverflowRow(LocalDate(2026, 1, 31), 1, 3, DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END, months(LocalDate(2026, 4, 30), 3)),
                OverflowRow(LocalDate(2026, 1, 31), 1, 3, DDayOverflowPolicyLocalEntity.SKIP, null),
                OverflowRow(LocalDate(2026, 1, 31), 1, 3, DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY, months(LocalDate(2026, 5, 1), 3)),
                OverflowRow(LocalDate(2024, 2, 29), 12, 1, DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END, years(LocalDate(2025, 2, 28), 1)),
                OverflowRow(LocalDate(2024, 2, 29), 12, 1, DDayOverflowPolicyLocalEntity.SKIP, null),
                OverflowRow(LocalDate(2024, 2, 29), 12, 1, DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY, years(LocalDate(2025, 3, 1), 1)),
                OverflowRow(LocalDate(2024, 2, 29), 12, 4, DDayOverflowPolicyLocalEntity.SKIP, years(LocalDate(2028, 2, 29), 4)),
            ).forEach { row ->
                val dday =
                    Arb
                        .localDDay(
                            baseDate = row.baseDate,
                            repeats = setOf(DateTimeUnit.MonthBased(months = row.months)),
                            overflowPolicy = row.policy,
                        ).next()
                val label =
                    if (row.months % 12 == 0) {
                        CalendarDDayKindLocalEntity.YEARS to row.order * row.months / 12
                    } else {
                        CalendarDDayKindLocalEntity.MONTHS to row.order * row.months
                    }

                anniversaryList(dday = dday, endInclusive = row.baseDate.plus(row.order * row.months + 1, DateTimeUnit.MONTH))
                    .filter { occurrence -> occurrence.kind to occurrence.amount == label } shouldBe listOfNotNull(row.expected)
            }
        }

        test("TC-203e4926-5d9b-44c9-a4b6-05a56f1a1fe6 표시하지 않은 달이 있어도 기념일의 번째를 건너뛰지 않는다") {
            val dday =
                Arb
                    .localDDay(
                        baseDate = LocalDate(2026, 1, 31),
                        repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                        overflowPolicy = DDayOverflowPolicyLocalEntity.SKIP,
                    ).next()

            anniversaryList(dday = dday, endInclusive = LocalDate(2026, 5, 31)) shouldBe
                listOf(months(LocalDate(2026, 3, 31), 2), months(LocalDate(2026, 5, 31), 4))
        }

        test("TC-66f8a1a7-b2fc-44b9-a904-e268ab16e6fa 일 간격의 기념일은 달력에 없는 날짜 처리를 따르지 않는다") {
            DDayOverflowPolicyLocalEntity.entries.forEach { policy ->
                val dday =
                    Arb
                        .localDDay(
                            baseDate = LocalDate(2026, 1, 31),
                            countsBaseDay = false,
                            repeats = setOf(DateTimeUnit.DayBased(days = 30)),
                            overflowPolicy = policy,
                        ).next()

                anniversaryList(dday = dday, endInclusive = LocalDate(2026, 3, 2)) shouldBe listOf(days(LocalDate(2026, 3, 2), 30))
            }
        }

        test("TC-32fae909-9054-455b-bb1e-558836609aa2 종료일이 있으면 종료일과 같거나 그 앞의 기념일만 있다") {
            val baseDate = LocalDate(2026, 1, 15)
            listOf(
                LocalDate(2026, 3, 15) to listOf(months(LocalDate(2026, 2, 15), 1), months(LocalDate(2026, 3, 15), 2)),
                LocalDate(2026, 3, 14) to listOf(months(LocalDate(2026, 2, 15), 1)),
                baseDate to emptyList(),
                null to (1..11).map { month -> months(baseDate.plus(month, DateTimeUnit.MONTH), month) },
            ).forEach { (endDate, expected) ->
                val dday = Arb.localDDay(baseDate = baseDate, repeats = setOf(DateTimeUnit.MonthBased(months = 1)), endDate = endDate).next()

                anniversaryList(dday = dday, endInclusive = LocalDate(2026, 12, 31)) shouldBe expected
            }
        }

        test("TC-773a1177-c4c4-4e35-917a-7102b7f1349c 종료일이 없으면 기념일은 끝없이 이어진다") {
            val baseDate = LocalDate(2026, 1, 1)
            val endInclusive = LocalDate(2126, 12, 31)
            val dday = Arb.localDDay(baseDate = baseDate, countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 100))).next()
            val expected =
                generateSequence(1) { order -> order + 1 }
                    .map { order -> days(baseDate.plus(order * 100 - 1, DateTimeUnit.DAY), order * 100) }
                    .takeWhile { occurrence -> occurrence.date <= endInclusive }
                    .toList()

            val anniversaryList = anniversaryList(dday = dday, endInclusive = endInclusive)

            anniversaryList.first() shouldBe days(LocalDate(2026, 4, 10), 100)
            anniversaryList shouldBe expected
        }

        test("TC-ee367607-64d4-41ff-83f9-3489ba31ed53 다음 달 1일로 옮겨진 기념일은 옮겨진 날짜로 종료일과 비교한다") {
            listOf(
                Triple(DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY, LocalDate(2026, 2, 28), null),
                Triple(DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY, LocalDate(2026, 3, 1), months(LocalDate(2026, 3, 1), 1)),
                Triple(DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END, LocalDate(2026, 2, 28), months(LocalDate(2026, 2, 28), 1)),
            ).forEach { (policy, endDate, expected) ->
                val dday =
                    Arb
                        .localDDay(
                            baseDate = LocalDate(2026, 1, 31),
                            repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                            endDate = endDate,
                            overflowPolicy = policy,
                        ).next()

                anniversaryList(dday = dday, endInclusive = LocalDate(2026, 3, 31))
                    .filter { occurrence -> occurrence.amount == 1 } shouldBe listOfNotNull(expected)
            }
        }

        test("TC-ed3371af-29ef-423e-bde8-8ba2ae68c285 같은 날짜의 기념일은 하나로 합치고 주년, 개월, 일 순으로 앞선 문구를 쓴다") {
            val baseDate = LocalDate(2026, 1, 1)
            listOf(
                setOf(DateTimeUnit.DayBased(days = 10), DateTimeUnit.DayBased(days = 12)) to days(LocalDate(2026, 3, 2), 60),
                setOf(DateTimeUnit.MonthBased(months = 6), DateTimeUnit.MonthBased(months = 12)) to years(LocalDate(2027, 1, 1), 1),
                setOf(DateTimeUnit.MonthBased(months = 3), DateTimeUnit.MonthBased(months = 12)) to years(LocalDate(2027, 1, 1), 1),
                setOf(DateTimeUnit.DayBased(days = 31), DateTimeUnit.MonthBased(months = 1)) to months(LocalDate(2026, 2, 1), 1),
                setOf(DateTimeUnit.DayBased(days = 365), DateTimeUnit.MonthBased(months = 12)) to years(LocalDate(2027, 1, 1), 1),
                setOf(DateTimeUnit.DayBased(days = 365), DateTimeUnit.MonthBased(months = 6), DateTimeUnit.MonthBased(months = 12)) to years(LocalDate(2027, 1, 1), 1),
            ).forEach { (repeats, expected) ->
                val dday = Arb.localDDay(baseDate = baseDate, countsBaseDay = false, repeats = repeats).next()

                anniversaryList(dday = dday, start = expected.date, endInclusive = expected.date) shouldBe listOf(expected)
            }
        }

        test("TC-e79c1260-420b-4217-b0dc-ebe4f550c66b 다른 디데이의 기념일은 같은 날짜라도 합치지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val date = LocalDate(2026, 2, 1)
            val monthly = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next()
            val daily = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), countsBaseDay = false, repeats = setOf(DateTimeUnit.DayBased(days = 31))).next()
            insert(accountId = accountId, dday = monthly)
            insert(accountId = accountId, dday = daily)

            dataSource
                .get(accountId = accountId, dateRange = date..date)
                .first()
                .associate { calendarDDay -> calendarDDay.ddayId to calendarDDay.toOccurrence() } shouldBe
                mapOf(monthly.id to months(date, 1), daily.id to days(date, 31))
        }

        test("TC-af3ea141-66e6-44c6-9c73-ae80fb735d93 모든 디데이는 종료일과 반복 간격과 관계없이 기준일 당일을 차지한다") {
            val baseDate = LocalDate(2026, 1, 15)
            listOf(
                emptySet<DateTimeUnit.DateBased>() to null,
                setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12)) to null,
                emptySet<DateTimeUnit.DateBased>() to baseDate,
                setOf(DateTimeUnit.MonthBased(months = 1)) to LocalDate(2026, 3, 31),
            ).forEach { (repeats, endDate) ->
                val accountId = Arb.kotlinUuid().next()
                insert(accountId = accountId, dday = Arb.localDDay(baseDate = baseDate, repeats = repeats, endDate = endDate).next())

                occurrenceList(accountId = accountId, start = baseDate, endInclusive = baseDate) shouldBe listOf(baseDay(baseDate))
            }
        }

        test("TC-04fb0dbc-1d1f-4d6a-90c3-92458083196a 기념일이 기준일 당일에 걸리면 기준일 당일 하나로 합친다") {
            val accountId = Arb.kotlinUuid().next()
            val baseDate = LocalDate(2026, 1, 15)
            insert(
                accountId = accountId,
                dday = Arb.localDDay(baseDate = baseDate, countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 1), DateTimeUnit.DayBased(days = 10))).next(),
            )

            occurrenceList(accountId = accountId, start = baseDate, endInclusive = LocalDate(2026, 1, 16)) shouldBe
                listOf(baseDay(baseDate), days(LocalDate(2026, 1, 16), 2))
        }

        // 추가 테스트: 기간 시작일이 기념일 날짜와 같거나 하루 뒤일 때 경계에서 빠지거나 겹치지 않는다.
        test("표시 대상 기간의 시작일 경계에 걸린 기념일은 그 기간에만 담긴다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 100))).next()
            insert(accountId = accountId, dday = dday)
            val anniversaryDate = LocalDate(2026, 4, 10)

            occurrenceList(accountId = accountId, start = anniversaryDate, endInclusive = anniversaryDate.plus(1, DateTimeUnit.DAY)) shouldBe
                listOf(days(anniversaryDate, 100))
            occurrenceList(accountId = accountId, start = anniversaryDate.plus(1, DateTimeUnit.DAY), endInclusive = LocalDate(2026, 7, 18)).shouldBeEmpty()
            occurrenceList(accountId = accountId, start = LocalDate(2026, 1, 2), endInclusive = anniversaryDate.minus(1, DateTimeUnit.DAY)).shouldBeEmpty()
        }

        // 추가 테스트: 다음 달 1일로 미룬 기념일이 기간 시작일과 같은 날이어도 담긴다.
        test("다음 달 1일로 미룬 기념일이 표시 대상 기간 시작일이면 담긴다") {
            val accountId = Arb.kotlinUuid().next()
            insert(
                accountId = accountId,
                dday =
                    Arb
                        .localDDay(
                            baseDate = LocalDate(2026, 1, 31),
                            repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                            overflowPolicy = DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY,
                        ).next(),
            )

            occurrenceList(accountId = accountId, start = LocalDate(2026, 3, 1), endInclusive = LocalDate(2026, 3, 1)) shouldBe
                listOf(months(LocalDate(2026, 3, 1), 1))
        }
    }) {
    private data class Row(
        val interval: Int,
        val countsBaseDay: Boolean,
        val expected: CalendarDDayOccurrence,
    )

    private data class OverflowRow(
        val baseDate: LocalDate,
        val months: Int,
        val order: Int,
        val policy: DDayOverflowPolicyLocalEntity,
        val expected: CalendarDDayOccurrence?,
    )
}
