package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.CalendarDDayOccurrence
import io.github.taetae98coding.diary.core.database.impl.dday.baseDay
import io.github.taetae98coding.diary.core.database.impl.dday.days
import io.github.taetae98coding.diary.core.database.impl.dday.months
import io.github.taetae98coding.diary.core.database.impl.dday.toOccurrence
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.years
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class AccountCalendarDDayLocalDataSourceImplTest :
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
            vararg ddayList: DDayLocalEntity,
        ) {
            ddayList.forEach { dday -> transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList()) }
        }

        suspend fun calendarList(
            accountId: Uuid,
            start: LocalDate = RANGE_START,
            endInclusive: LocalDate = RANGE_END_INCLUSIVE,
        ): List<CalendarDDayLocalEntity> = dataSource.get(accountId = accountId, dateRange = start..endInclusive).first()

        suspend fun occurrenceList(
            accountId: Uuid,
            start: LocalDate = RANGE_START,
            endInclusive: LocalDate = RANGE_END_INCLUSIVE,
        ): List<CalendarDDayOccurrence> = calendarList(accountId = accountId, start = start, endInclusive = endInclusive).map { calendarDDay -> calendarDDay.toOccurrence() }

        // 구독 직전의 쓰기도 Room의 무효화 알림이 늦게 와 같은 결과를 한 번 더 전달할 수 있으므로, 바뀐 결과만 남겨 다음 전달을 기다린다.
        fun occurrenceFlow(
            accountId: Uuid,
            start: LocalDate,
            endInclusive: LocalDate,
        ): Flow<List<CalendarDDayOccurrence>> =
            dataSource
                .get(accountId = accountId, dateRange = start..endInclusive)
                .map { calendarList -> calendarList.map { calendarDDay -> calendarDDay.toOccurrence() } }
                .distinctUntilChanged()

        suspend fun updateDetail(
            accountId: Uuid,
            dday: DDayLocalEntity,
        ) {
            transaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = dday.detail, updatedAt = Arb.secondPrecisionInstant().next())
        }

        test("TC-bbd24f37-a550-4615-9b94-11b4c8506186 표시 대상 기간에 드는 날짜를 차지하는 디데이만 표시 대상이 된다") {
            val accountId = Arb.kotlinUuid().next()
            val ddayList =
                listOf(
                    LocalDate(2026, 6, 27) to false,
                    LocalDate(2026, 6, 28) to true,
                    LocalDate(2026, 7, 15) to true,
                    LocalDate(2026, 8, 8) to true,
                    LocalDate(2026, 8, 9) to false,
                ).map { (baseDate, isIncluded) -> Arb.localDDay(baseDate = baseDate).next() to isIncluded }
            insert(accountId, *ddayList.map { (dday, _) -> dday }.toTypedArray())

            calendarList(accountId = accountId).map { calendarDDay -> calendarDDay.ddayId to calendarDDay.toOccurrence() } shouldBe
                ddayList
                    .filter { (_, isIncluded) -> isIncluded }
                    .map { (dday, _) -> dday.id to baseDay(dday.detail.baseDate) }
        }

        test("TC-ae489af4-df9d-41f3-9a52-4514679f622f 캘린더 디데이는 식별값, 제목, 컬러, 차지하는 하루와 문구를 가진다") {
            val accountId = Arb.kotlinUuid().next()
            val baseDate = LocalDate(2026, 11, 19)
            val dday =
                Arb.localDDay(baseDate = baseDate, title = "수능").next().let { value ->
                    value.copy(detail = value.detail.copy(color = RED))
                }
            insert(accountId, dday)

            calendarList(accountId = accountId, start = LocalDate(2026, 11, 1), endInclusive = LocalDate(2026, 12, 12)) shouldBe
                listOf(
                    CalendarDDayLocalEntity(
                        ddayId = dday.id,
                        title = "수능",
                        color = RED,
                        date = baseDate,
                        kind = CalendarDDayKindLocalEntity.BASE_DAY,
                        amount = 0,
                    ),
                )
        }

        test("TC-dedfcb3e-49c2-43bf-b8d3-66896541c1d0 기념일은 그 날짜 하루를 차지하는 캘린더 디데이로 전달된다") {
            listOf(
                Arb.localDDay(baseDate = LocalDate(2026, 1, 15), repeats = setOf(DateTimeUnit.MonthBased(months = 1)), title = "아기").next() to
                    months(LocalDate(2026, 7, 15), 6),
                Arb.localDDay(baseDate = LocalDate(2025, 7, 10), repeats = setOf(DateTimeUnit.MonthBased(months = 12)), title = "결혼").next() to
                    years(LocalDate(2026, 7, 10), 1),
                Arb.localDDay(baseDate = LocalDate(2026, 4, 1), countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 100)), title = "우리").next() to
                    days(LocalDate(2026, 7, 9), 100),
            ).forEach { (dday, expected) ->
                val accountId = Arb.kotlinUuid().next()
                insert(accountId, dday)

                occurrenceList(accountId = accountId) shouldBe listOf(expected)
            }
        }

        test("TC-9e16c5af-f644-4b6b-b858-6fca38b24ef7 한 디데이가 같은 날짜에 여러 번 걸려도 그 날짜에는 하나만 전달된다") {
            listOf(
                Arb.localDDay(baseDate = LocalDate(2025, 7, 10), repeats = setOf(DateTimeUnit.MonthBased(months = 6), DateTimeUnit.MonthBased(months = 12))).next() to
                    years(LocalDate(2026, 7, 10), 1),
                Arb.localDDay(baseDate = LocalDate(2026, 7, 10), countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 1))).next() to
                    baseDay(LocalDate(2026, 7, 10)),
            ).forEach { (dday, expected) ->
                val accountId = Arb.kotlinUuid().next()
                insert(accountId, dday)

                occurrenceList(accountId = accountId)
                    .filter { occurrence -> occurrence.date == expected.date } shouldBe listOf(expected)
            }
        }

        test("TC-3ea576e3-de1c-4086-90e4-fbe469e97793 다른 디데이의 같은 날짜는 합치지 않고 날짜, 제목, 식별값 순으로 전달한다") {
            val accountId = Arb.kotlinUuid().next()
            val (smallId, largeId) = List(2) { Arb.kotlinUuid().next() }.sortedBy { id -> id.toHexString() }
            val ga = Arb.localDDay(baseDate = LocalDate(2026, 7, 20), title = "가").next()
            val da = Arb.localDDay(baseDate = LocalDate(2026, 7, 10), title = "다").next()
            val naSmall = Arb.localDDay(baseDate = LocalDate(2026, 7, 10), title = "나").next().copy(id = smallId)
            val naLarge = Arb.localDDay(baseDate = LocalDate(2026, 7, 10), title = "나").next().copy(id = largeId)
            insert(accountId, ga, naLarge, da, naSmall)

            calendarList(accountId = accountId).map { calendarDDay -> calendarDDay.ddayId } shouldBe
                listOf(naSmall.id, naLarge.id, da.id, ga.id)
        }

        test("TC-3f2675de-ccef-40d6-a4dd-e26494cbe026 같은 표시 대상 기간을 다시 조회해도 순서가 유지된다") {
            val accountId = Arb.kotlinUuid().next()
            insert(
                accountId,
                Arb.localDDay(baseDate = LocalDate(2026, 7, 1), repeats = setOf(DateTimeUnit.DayBased(days = 10))).next(),
                Arb.localDDay(baseDate = LocalDate(2026, 6, 1), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next(),
                Arb.localDDay(baseDate = LocalDate(2026, 7, 11)).next(),
                Arb.localDDay(baseDate = LocalDate(2026, 7, 11)).next(),
            )

            val first = calendarList(accountId = accountId)
            val second = calendarList(accountId = accountId)

            first.size shouldBe second.size
            second shouldBe first
        }

        test("TC-84a5428a-c970-40c5-91c7-af7075d8ab97 삭제된 디데이와 다른 계정의 디데이는 표시하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val baseDate = LocalDate(2026, 7, 15)
            val exam = Arb.localDDay(baseDate = baseDate, title = "수능").next()
            insert(accountId, exam, Arb.localDDay(baseDate = baseDate, title = "토익", isDeleted = true).next())
            insert(otherAccountId, Arb.localDDay(baseDate = baseDate, title = "면접").next())

            calendarList(accountId = accountId).map { calendarDDay -> calendarDDay.ddayId to calendarDDay.title } shouldBe listOf(exam.id to "수능")
        }

        test("TC-df959a1b-4ac9-44ac-8d03-48809717c9c8 종료일이 지난 디데이도 종료일까지는 표시하고 그 뒤에는 표시하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            insert(
                accountId,
                Arb
                    .localDDay(
                        baseDate = LocalDate(2026, 1, 15),
                        repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                        endDate = LocalDate(2026, 3, 31),
                        title = "수업",
                    ).next(),
            )

            occurrenceList(accountId = accountId, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 5, 31)) shouldBe
                listOf(baseDay(LocalDate(2026, 1, 15)), months(LocalDate(2026, 2, 15), 1), months(LocalDate(2026, 3, 15), 2))
        }

        test("TC-1585e9de-dc34-437c-b327-fab6b530fb41 표시 대상 기간이 바뀌면 그 기간을 기준으로 다시 정해진다") {
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, Arb.localDDay(baseDate = LocalDate(2026, 1, 15), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next())
            occurrenceList(accountId = accountId) shouldBe listOf(months(LocalDate(2026, 7, 15), 6))

            occurrenceList(accountId = accountId, start = LocalDate(2026, 7, 26), endInclusive = LocalDate(2026, 9, 5)) shouldBe
                listOf(months(LocalDate(2026, 8, 15), 7))
        }

        test("TC-f8c915ac-082c-44d4-9228-99011f362445 디데이가 추가되거나 제목·컬러가 바뀌면 조작 없이 결과에 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val exam = Arb.localDDay(baseDate = LocalDate(2026, 7, 15), title = "수능").next().let { value -> value.copy(detail = value.detail.copy(color = RED)) }
            val toeic = Arb.localDDay(baseDate = LocalDate(2026, 7, 20), title = "토익").next()
            insert(accountId, exam)

            dataSource.get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE).distinctUntilChanged().test {
                awaitItem().map { calendarDDay -> calendarDDay.title } shouldBe listOf("수능")

                insert(accountId, toeic)
                awaitItem().map { calendarDDay -> calendarDDay.ddayId to calendarDDay.title } shouldBe listOf(exam.id to "수능", toeic.id to "토익")

                val renamed = exam.copy(detail = exam.detail.copy(title = "시험"))
                updateDetail(accountId = accountId, dday = renamed)
                awaitItem().first { calendarDDay -> calendarDDay.ddayId == exam.id }.let { calendarDDay ->
                    calendarDDay.title shouldBe "시험"
                    calendarDDay.date shouldBe exam.detail.baseDate
                }

                updateDetail(accountId = accountId, dday = renamed.copy(detail = renamed.detail.copy(color = BLUE)))
                awaitItem().first { calendarDDay -> calendarDDay.ddayId == exam.id }.let { calendarDDay ->
                    calendarDDay.color shouldBe BLUE
                    calendarDDay.date shouldBe exam.detail.baseDate
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-0382ed67-7991-48d4-b461-fc5ae058d82b 오늘이 바뀌어도 캘린더 디데이는 달라지지 않는다") {
            // 조회는 오늘을 입력으로 받지 않으므로, 오늘이 2026년 6월 30일일 때와 7월 20일일 때의 조회는 같은 기간의 두 번 조회와 같다.
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, Arb.localDDay(baseDate = LocalDate(2026, 7, 1), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next())
            val expected = listOf(baseDay(LocalDate(2026, 7, 1)), months(LocalDate(2026, 8, 1), 1))

            occurrenceList(accountId = accountId) shouldBe expected
            occurrenceList(accountId = accountId) shouldBe expected
        }

        test("TC-dfea8cb8-9a9a-416a-befc-d9005745328b 표시할 디데이가 없으면 빈 결과를 전달한다") {
            val emptyAccountId = Arb.kotlinUuid().next()
            calendarList(accountId = emptyAccountId).shouldBeEmpty()

            val accountId = Arb.kotlinUuid().next()
            insert(
                accountId,
                Arb.localDDay(baseDate = LocalDate(2026, 8, 9), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next(),
                Arb.localDDay(baseDate = LocalDate(2026, 1, 1), repeats = setOf(DateTimeUnit.MonthBased(months = 1)), endDate = LocalDate(2026, 6, 27)).next(),
            )
            calendarList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-05fb90aa-e006-4577-a3e5-11b45e65c276 먼 미래의 표시 대상 기간에도 그 기간의 기념일을 담는다") {
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, Arb.localDDay(baseDate = LocalDate(2026, 7, 10), repeats = setOf(DateTimeUnit.MonthBased(months = 12)), title = "결혼").next())

            occurrenceList(accountId = accountId, start = LocalDate(2099, 6, 28), endInclusive = LocalDate(2099, 8, 8)) shouldBe
                listOf(years(LocalDate(2099, 7, 10), 73))
        }

        test("TC-4c6d86de-72b1-4c4f-bca6-cc58425f22b6 연도 경계를 넘는 표시 대상 기간은 두 연도의 기념일을 함께 담는다") {
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, Arb.localDDay(baseDate = LocalDate(2026, 5, 20), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next())

            occurrenceList(accountId = accountId, start = LocalDate(2026, 12, 13), endInclusive = LocalDate(2027, 1, 23)) shouldBe
                listOf(months(LocalDate(2026, 12, 20), 7), months(LocalDate(2027, 1, 20), 8))
        }

        test("TC-b3550d34-2b12-4e62-9b3d-9ffaa777b712 디데이가 삭제되면 그 디데이의 캘린더 디데이가 모두 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(baseDate = LocalDate(2026, 7, 1), repeats = setOf(DateTimeUnit.DayBased(days = 10))).next()
            val other = Arb.localDDay(baseDate = LocalDate(2026, 7, 2)).next()
            insert(accountId, dday, other)

            dataSource.get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE).distinctUntilChanged().test {
                awaitItem().filter { calendarDDay -> calendarDDay.ddayId == dday.id } shouldHaveSize 4

                transaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

                awaitItem().map { calendarDDay -> calendarDDay.ddayId } shouldBe listOf(other.id)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-e91c880f-3331-4927-a529-99c44f6c5022 날짜를 정하는 값이 바뀌면 다시 정한 날짜와 문구가 전달된다") {
            val start = LocalDate(2026, 3, 29)
            val endInclusive = LocalDate(2026, 5, 9)
            listOf<Pair<(DDayDetailLocalEntity) -> DDayDetailLocalEntity, List<CalendarDDayOccurrence>>>(
                { detail: DDayDetailLocalEntity -> detail.copy(countsBaseDay = false) } to listOf(days(LocalDate(2026, 4, 11), 100)),
                { detail: DDayDetailLocalEntity -> detail.copy(baseDate = LocalDate(2026, 1, 2)) } to listOf(days(LocalDate(2026, 4, 11), 100)),
                { detail: DDayDetailLocalEntity -> detail.copy(repeats = emptySet()) } to emptyList(),
                { detail: DDayDetailLocalEntity -> detail.copy(endDate = LocalDate(2026, 4, 9)) } to emptyList(),
            ).forEach { (change, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val dday = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 100))).next()
                val changed = change(dday.detail)
                insert(accountId, dday)

                occurrenceFlow(accountId = accountId, start = start, endInclusive = endInclusive).test {
                    awaitItem() shouldBe listOf(days(LocalDate(2026, 4, 10), 100))

                    updateDetail(accountId = accountId, dday = dday.copy(detail = changed))

                    awaitItem() shouldBe expected
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-e5986f21-11df-4faa-ab95-c93dc4733401 달력에 없는 날짜 처리가 바뀌면 그 처리로 정한 날짜가 전달된다") {
            val start = LocalDate(2026, 2, 1)
            val endInclusive = LocalDate(2026, 3, 14)
            listOf(
                DDayOverflowPolicyLocalEntity.SKIP to emptyList(),
                DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY to listOf(months(LocalDate(2026, 3, 1), 1)),
            ).forEach { (policy, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val dday = Arb.localDDay(baseDate = LocalDate(2026, 1, 31), repeats = setOf(DateTimeUnit.MonthBased(months = 1))).next()
                insert(accountId, dday)

                occurrenceFlow(accountId = accountId, start = start, endInclusive = endInclusive).test {
                    awaitItem() shouldBe listOf(months(LocalDate(2026, 2, 28), 1))

                    updateDetail(accountId = accountId, dday = dday.copy(detail = dday.detail.copy(overflowPolicy = policy)))

                    awaitItem() shouldBe expected
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-ada464d7-89c8-4080-bc99-b667f7196932 태그 연결은 캘린더의 디데이 표시를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(baseDate = LocalDate(2026, 7, 1), repeats = setOf(DateTimeUnit.DayBased(days = 10))).next()
            val tag = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)
            insert(accountId, dday, Arb.localDDay(baseDate = LocalDate(2026, 7, 5)).next())
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val before = calendarList(accountId = accountId)

            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = dday.id,
                tagId = tag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            calendarList(accountId = accountId) shouldBe before
        }
    }) {
    private companion object {
        val RANGE_START: LocalDate = LocalDate(2026, 6, 28)
        val RANGE_END_INCLUSIVE: LocalDate = LocalDate(2026, 8, 8)
        const val RED: Long = 0xFFFF0000
        const val BLUE: Long = 0xFF0000FF
    }
}
