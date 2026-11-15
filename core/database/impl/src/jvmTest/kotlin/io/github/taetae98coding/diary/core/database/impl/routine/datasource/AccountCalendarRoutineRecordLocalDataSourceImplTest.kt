package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.CALENDAR_RANGE_END_INCLUSIVE
import io.github.taetae98coding.diary.core.database.impl.routine.CALENDAR_RANGE_START
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.routineUnit
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

// 캘린더 루틴 기록을 차지하는 날짜와 그 날짜 합계로 줄여 비교한다.
private data class DateAmount(
    val date: LocalDate,
    val amount: Long,
)

private fun CalendarRoutineRecordLocalEntity.toDateAmount(): DateAmount = DateAmount(date = date, amount = amount)

private fun july(
    day: Int,
    amount: Long,
): DateAmount = DateAmount(date = LocalDate(2026, 7, day), amount = amount)

class AccountCalendarRoutineRecordLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountCalendarRoutineRecordLocalDataSourceImpl

        beforeTest {
            database = routineTestDatabase()
            dataSource = AccountCalendarRoutineRecordLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWater(
            accountId: Uuid,
            title: String? = null,
            isDeleted: Boolean = false,
        ): RoutineLocalEntity {
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = title, isDeleted = isDeleted).next()
            database.insertRoutine(accountId, routine)

            return routine
        }

        suspend fun record(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            date: LocalDate,
            amount: Int,
            isDeleted: Boolean = false,
        ) = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = date, amount = amount, isDeleted = isDeleted)

        suspend fun dateAmountList(
            accountId: Uuid,
            start: LocalDate = CALENDAR_RANGE_START,
            endInclusive: LocalDate = CALENDAR_RANGE_END_INCLUSIVE,
        ): List<DateAmount> = database.calendarRoutineRecordList(accountId = accountId, start = start, endInclusive = endInclusive).map { calendarRecord -> calendarRecord.toDateAmount() }

        // 구독 직전의 쓰기도 Room의 무효화 알림이 늦게 와 같은 결과를 한 번 더 전달할 수 있으므로, 바뀐 결과만 남겨 다음 전달을 기다린다.
        fun calendarFlow(accountId: Uuid): Flow<List<CalendarRoutineRecordLocalEntity>> =
            dataSource
                .get(accountId = accountId, dateRange = CALENDAR_RANGE_START..CALENDAR_RANGE_END_INCLUSIVE)
                .distinctUntilChanged()

        fun dateAmountFlow(accountId: Uuid): Flow<List<DateAmount>> = calendarFlow(accountId = accountId).map { list -> list.map { calendarRecord -> calendarRecord.toDateAmount() } }

        suspend fun updateDetail(
            accountId: Uuid,
            routine: RoutineLocalEntity,
        ) {
            AccountRoutineTransactionImpl(database = database).updateDetail(
                accountId = accountId,
                routineId = routine.id,
                detail = routine.detail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1
        }

        test("TC-407dac03-4c85-447f-b14b-bdc62414009e TC-15563460-fe46-4c90-912b-a04b3644dc2a 표시 대상 기간에 드는 날짜의 기록만 표시 대상이 된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            listOf(
                LocalDate(2026, 6, 27),
                LocalDate(2026, 6, 28),
                LocalDate(2026, 7, 1),
                LocalDate(2026, 7, 15),
                LocalDate(2026, 7, 31),
                LocalDate(2026, 8, 8),
                LocalDate(2026, 8, 9),
            ).forEach { date -> record(accountId, routine, date = date, amount = 500) }

            dateAmountList(accountId) shouldBe
                listOf(
                    DateAmount(LocalDate(2026, 6, 28), 500),
                    july(1, 500),
                    july(15, 500),
                    july(31, 500),
                    DateAmount(LocalDate(2026, 8, 8), 500),
                )
            database.calendarRoutineRecordList(accountId).map { calendarRecord -> calendarRecord.unit }.distinct() shouldBe listOf(routine.detail.unit)
        }

        test("TC-02e860c4-c7b1-43ad-aff5-abfd981cd4e7 캘린더 루틴 기록은 식별값, 제목, 루틴 컬러, 차지하는 하루와 그 날짜 합계와 단위를 가진다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 1500)

            database.calendarRoutineRecordList(accountId) shouldBe
                listOf(
                    CalendarRoutineRecordLocalEntity(
                        routineId = routine.id,
                        title = routine.detail.title,
                        color = routine.detail.color,
                        date = LocalDate(2026, 7, 10),
                        amount = 1500,
                        unit = routine.detail.unit,
                    ),
                )
        }

        test("TC-70ce7650-3501-4a73-85ef-1146c2e1f80e TC-c785ccdf-600f-47ec-b068-48e1be546774 한 루틴의 같은 날짜 기록은 양을 모두 더한 하나가 되고 날짜가 다르면 날짜마다 하나씩 되며 기록이 없는 날짜에는 없다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 300)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 200)
            record(accountId, routine, date = LocalDate(2026, 7, 12), amount = 250)

            dateAmountList(accountId, start = LocalDate(2026, 7, 10), endInclusive = LocalDate(2026, 7, 12)) shouldBe listOf(july(10, 1000), july(12, 250))
        }

        test("TC-98f13ea0-a0f8-478d-9865-f2d51ecde490 TC-f13a1c48-7e28-46e0-b777-644a88b56b35 다른 루틴의 같은 날짜는 합치지 않고 날짜, 제목, 식별값 순으로 전달한다") {
            val accountId = Arb.kotlinUuid().next()
            val (smallId, largeId) = List(2) { Arb.kotlinUuid().next() }.sortedBy { id -> id.toHexString() }
            val ga = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "가").next()
            val da = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "다").next()
            val naSmall = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "나").next().copy(id = smallId)
            val naLarge = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "나").next().copy(id = largeId)
            database.insertRoutine(accountId, ga, naLarge, da, naSmall)
            record(accountId, ga, date = LocalDate(2026, 7, 20), amount = 1)
            record(accountId, da, date = LocalDate(2026, 7, 10), amount = 1)
            record(accountId, naLarge, date = LocalDate(2026, 7, 10), amount = 1)
            record(accountId, naSmall, date = LocalDate(2026, 7, 10), amount = 1)

            database.calendarRoutineRecordList(accountId).map { calendarRecord -> calendarRecord.routineId } shouldBe listOf(naSmall.id, naLarge.id, da.id, ga.id)
        }

        test("TC-8f9fe355-248b-46ce-aee4-3075f115efb3 같은 표시 대상 기간을 다시 조회해도 순서가 유지된다") {
            val accountId = Arb.kotlinUuid().next()
            List(4) { insertWater(accountId, title = "루틴") }.forEachIndexed { index, routine ->
                record(accountId, routine, date = LocalDate(2026, 7, 10), amount = index + 1)
                record(accountId, routine, date = LocalDate(2026, 7, 11 + index), amount = index + 1)
            }

            val first = database.calendarRoutineRecordList(accountId)
            val second = database.calendarRoutineRecordList(accountId)

            first.size shouldBe 8
            second shouldBe first
        }

        test("TC-3b22003a-9dba-420f-a043-dbfee7075a18 삭제된 루틴의 기록과 다른 계정의 루틴 기록은 표시하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val date = LocalDate(2026, 7, 15)
            val water = insertWater(accountId)
            val deleted = insertWater(accountId, isDeleted = true)
            val other = insertWater(otherAccountId)
            record(accountId, water, date = date, amount = 1)
            record(accountId, deleted, date = date, amount = 1)
            record(otherAccountId, other, date = date, amount = 1)

            database.calendarRoutineRecordList(accountId).map { calendarRecord -> calendarRecord.routineId to calendarRecord.title } shouldBe listOf(water.id to water.detail.title)
        }

        test("TC-3d9fd53e-33ae-4515-8b6f-a2df1fda3140 TC-1564f0b4-ee10-404a-9a99-53e65e0fae30 삭제된 기록은 합계에 넣지 않고 한 날짜의 기록이 모두 삭제되었으면 그 날짜에 없다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 700, isDeleted = true)
            record(accountId, routine, date = LocalDate(2026, 7, 11), amount = 300, isDeleted = true)

            dateAmountList(accountId) shouldBe listOf(july(10, 500))
        }

        test("TC-02241816-994f-49e6-8e18-693974dfb580 삭제된 루틴의 기록과 삭제된 기록은 달력에 표시하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routineX = insertWater(accountId, isDeleted = true)
            val routineY = insertWater(accountId)
            record(accountId, routineX, date = LocalDate(2026, 7, 10), amount = 100)
            record(accountId, routineY, date = LocalDate(2026, 7, 10), amount = 100, isDeleted = true)
            record(accountId, routineY, date = LocalDate(2026, 7, 12), amount = 500)
            record(accountId, routineY, date = LocalDate(2026, 7, 12), amount = 300, isDeleted = true)

            database.calendarRoutineRecordList(accountId).map { calendarRecord -> Triple(calendarRecord.routineId, calendarRecord.date, calendarRecord.amount) } shouldBe
                listOf(Triple(routineY.id, LocalDate(2026, 7, 12), 500L))
        }

        test("TC-7ea70626-7100-4c6a-b5bc-ffb1aecd4a7c 루틴의 진행 상태와 시작 날짜는 노출 기준에 쓰지 않는다") {
            listOf(
                LocalDate(2026, 8, 1) to LocalDate(2026, 8, 3),
                LocalDate(2026, 6, 1) to LocalDate(2026, 7, 1),
                LocalDate(2026, 7, 12) to LocalDate(2026, 7, 10),
                LocalDate(2026, 6, 1) to LocalDate(2026, 7, 20),
            ).forEach { (startDate, recordDate) ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(startDate = startDate).next()
                database.insertRoutine(accountId, routine)
                record(accountId, routine, date = recordDate, amount = 500)

                dateAmountList(accountId) shouldBe listOf(DateAmount(date = recordDate, amount = 500))
            }
        }

        test("TC-881762ca-cfe6-4dfd-92a9-7c057495c087 날짜별 합계는 주기, 목표량, 진행 상태와 오늘의 영향을 받지 않는다") {
            // 조회는 오늘을 입력으로 받지 않으므로 오늘과 그에 따른 진행 상태가 달라도 같은 조회다.
            listOf(
                RoutineCycleLocalEntity.DAILY to null,
                RoutineCycleLocalEntity.WEEKLY to 3,
                RoutineCycleLocalEntity.MONTHLY to 100,
                RoutineCycleLocalEntity.DAILY to 3,
            ).forEach { (cycle, goalAmount) ->
                val accountId = Arb.kotlinUuid().next()
                val date = LocalDate(2026, 11, 10)
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 11, 1), cycle = cycle, unit = "count", goalAmount = goalAmount).next()
                database.insertRoutine(accountId, routine)
                record(accountId, routine, date = date, amount = 2)
                record(accountId, routine, date = date, amount = 3)

                database.calendarRoutineRecordList(accountId, start = date, endInclusive = date).map { calendarRecord -> calendarRecord.amount to calendarRecord.unit } shouldBe
                    listOf(5L to "count")
            }
        }

        test("TC-cfaa0be0-7043-4760-8586-d0c1e6051317 완료된 루틴의 기록도 완료되지 않은 루틴의 기록과 같이 표시한다") {
            val accountId = Arb.kotlinUuid().next()
            val routineList =
                listOf(
                    Arb.localRoutine(startDate = LocalDate(2026, 6, 1), title = "물 마시기", isFinished = false).next(),
                    Arb.localRoutine(startDate = LocalDate(2026, 6, 1), title = "운동", isFinished = true).next(),
                    Arb.localRoutine(startDate = LocalDate(2026, 8, 1), title = "달리기", isFinished = true).next(),
                )
            database.insertRoutine(accountId, *routineList.toTypedArray())
            routineList.forEach { routine -> record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500) }

            database.calendarRoutineRecordList(accountId).map { calendarRecord -> Triple(calendarRecord.routineId, calendarRecord.date, calendarRecord.amount) } shouldContainExactlyInAnyOrder
                routineList.map { routine -> Triple(routine.id, LocalDate(2026, 7, 10), 500L) }
        }

        test("TC-52c7e6f7-4da2-451d-ba2f-46eb317677ca 루틴을 완료하거나 다시 시작해도 캘린더 루틴 기록은 달라지지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            val transaction = AccountRoutineTransactionImpl(database = database)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 12), amount = 200)
            val before = database.calendarRoutineRecordList(accountId)
            before.map { calendarRecord -> calendarRecord.toDateAmount() } shouldBe listOf(july(10, 500), july(12, 200))

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            database.calendarRoutineRecordList(accountId) shouldBe before

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            database.calendarRoutineRecordList(accountId) shouldBe before
        }

        test("TC-d84d4913-9efe-44f4-9c08-9ee8a981dbcc 기록이 없는 날짜에는 목표를 채우지 못했어도 캘린더 루틴 기록을 두지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), goalAmount = 2000).next()
            database.insertRoutine(accountId, routine)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 1500)

            dateAmountList(accountId) shouldBe listOf(july(10, 1500))
        }

        test("TC-44fe465b-e2e1-47e1-8ec8-6913fc918e79 루틴의 설명, 시작 날짜, 주기, 목표량과 한 번에 기록할 양이 달라도 캘린더 루틴 기록은 같다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 1500)
            val before = database.calendarRoutineRecordList(accountId)
            before.map { calendarRecord -> calendarRecord.toDateAmount() } shouldBe listOf(july(10, 1500))

            updateDetail(
                accountId,
                routine.copy(
                    detail =
                        routine.detail.copy(
                            description = "${routine.detail.description}-changed",
                            startDate = LocalDate(2026, 8, 1),
                            cycle = RoutineCycleLocalEntity.MONTHLY,
                            goalAmount = 9000,
                            amountPerRecord = routine.detail.amountPerRecord % 9_999_999 + 1,
                        ),
                ),
            )

            database.calendarRoutineRecordList(accountId) shouldBe before
        }

        test("TC-288f1b02-3b05-4c74-8f59-c0a67b731135 표시 대상 기간이 바뀌면 그 기간을 기준으로 다시 정해진다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 15), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 8, 15), amount = 700)
            dateAmountList(accountId) shouldBe listOf(july(15, 500))

            dateAmountList(accountId, start = LocalDate(2026, 7, 26), endInclusive = LocalDate(2026, 9, 5)) shouldBe listOf(DateAmount(LocalDate(2026, 8, 15), 700))
        }

        test("TC-59e821b9-ed98-4803-9eec-dc58ef255078 기록이 추가되면 조작 없이 결과에 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            val transaction = AccountRoutineRecordTransactionImpl(database = database)

            dateAmountFlow(accountId).test {
                awaitItem() shouldBe listOf(july(10, 500))

                transaction.upsert(accountId = accountId, record = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 7, 10), amount = 300).next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 800))

                transaction.upsert(accountId = accountId, record = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 7, 12), amount = 200).next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 800), july(12, 200))

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-1294ed40-a0f5-4a6e-b3dd-e3daa9f51b38 루틴의 제목, 컬러나 단위가 바뀌면 조작 없이 결과에 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            val detail = routine.detail
            val title = Arb.string().filter { title -> title != detail.title }.next()
            val color = Arb.long().filter { color -> color != detail.color }.next()
            val unit = Arb.routineUnit().filter { unit -> unit != detail.unit && unit.isNotEmpty() }.next()
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)

            calendarFlow(accountId).map { list -> list.map { calendarRecord -> Triple(calendarRecord.title, calendarRecord.color, calendarRecord.unit) } }.test {
                awaitItem() shouldBe listOf(Triple(detail.title, detail.color, detail.unit))

                updateDetail(accountId, routine.copy(detail = detail.copy(title = title)))
                awaitItem() shouldBe listOf(Triple(title, detail.color, detail.unit))

                updateDetail(accountId, routine.copy(detail = detail.copy(title = title, color = color)))
                awaitItem() shouldBe listOf(Triple(title, color, detail.unit))

                updateDetail(accountId, routine.copy(detail = detail.copy(title = title, color = color, unit = unit)))
                awaitItem() shouldBe listOf(Triple(title, color, unit))

                updateDetail(accountId, routine.copy(detail = detail.copy(title = title, color = color, unit = "")))
                awaitItem() shouldBe listOf(Triple(title, color, ""))

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-00e09141-91c2-42ad-8658-9039d99b0649 단위가 비어 있는 루틴의 캘린더 루틴 기록은 빈 단위와 그 날짜의 합계를 가진다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "운동", unit = "").next()
            database.insertRoutine(accountId, routine)
            repeat(2) { record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 1) }

            val calendarRecord = database.calendarRoutineRecordList(accountId = accountId).single()

            calendarRecord.title shouldBe "운동"
            calendarRecord.date shouldBe LocalDate(2026, 7, 10)
            calendarRecord.amount shouldBe 2L
            calendarRecord.unit shouldBe ""
        }

        test("TC-851b4a31-3add-4070-8c3b-12b9ad78fd43 루틴의 주기와 하는 요일은 노출 기준에 쓰지 않는다") {
            listOf(
                RoutineCycleLocalEntity.WEEKDAYS to 11,
                RoutineCycleLocalEntity.WEEKDAYS to 12,
                RoutineCycleLocalEntity.WEEKDAYS to 10,
                RoutineCycleLocalEntity.WEEKENDS to 15,
                RoutineCycleLocalEntity.WEEKENDS to 11,
            ).forEach { (cycle, day) ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), cycle = cycle, unit = "ml").next()
                database.insertRoutine(accountId, routine)
                record(accountId, routine, date = LocalDate(2026, 7, day), amount = 500)

                database.calendarRoutineRecordList(accountId = accountId).map { calendarRecord -> Triple(calendarRecord.date, calendarRecord.amount, calendarRecord.unit) } shouldBe
                    listOf(Triple(LocalDate(2026, 7, day), 500L, "ml"))
            }
        }

        test("TC-fefd184e-3269-4751-a0c3-801dac995ee1 하는 요일에 들지 않는 날짜의 기록도 그 날짜의 날짜별 합계가 된다") {
            listOf(
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 11, 14),
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 11, 15),
                RoutineCycleLocalEntity.WEEKENDS to LocalDate(2026, 11, 13),
                RoutineCycleLocalEntity.WEEKENDS to LocalDate(2026, 11, 16),
            ).forEach { (cycle, date) ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), cycle = cycle, unit = "회").next()
                database.insertRoutine(accountId, routine)
                record(accountId, routine, date = date, amount = 2)
                record(accountId, routine, date = date, amount = 3)

                database.calendarRoutineRecordList(accountId = accountId, start = date, endInclusive = date).map { calendarRecord -> calendarRecord.amount to calendarRecord.unit } shouldBe
                    listOf(5L to "회")
            }
        }

        test("TC-59faa40b-a28c-4db1-9ce4-c2cd7a31cba7 오늘이 바뀌어도 캘린더 루틴 기록은 달라지지 않는다") {
            // 조회는 오늘을 입력으로 받지 않으므로 오늘 2026-06-30, 2026-07-05, 2026-07-20의 조회는 같은 조회를 세 번 한 것과 같다.
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 7, 1)).next()
            database.insertRoutine(accountId, routine)
            record(accountId, routine, date = LocalDate(2026, 7, 1), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 700)

            List(3) { dateAmountList(accountId) }.distinct() shouldBe listOf(listOf(july(1, 500), july(10, 700)))
        }

        test("TC-32e3c1eb-b9d8-493d-99c7-894aa39f1b9f 표시할 루틴 기록이 없으면 빈 결과를 전달한다") {
            val emptyAccountId = Arb.kotlinUuid().next()
            val noRecordAccountId = Arb.kotlinUuid().next()
            val outOfRangeAccountId = Arb.kotlinUuid().next()
            insertWater(noRecordAccountId)
            val routine = insertWater(outOfRangeAccountId)
            record(outOfRangeAccountId, routine, date = LocalDate(2026, 6, 27), amount = 1)
            record(outOfRangeAccountId, routine, date = LocalDate(2026, 8, 9), amount = 1)

            dateAmountList(emptyAccountId).shouldBeEmpty()
            dateAmountList(noRecordAccountId).shouldBeEmpty()
            dateAmountList(outOfRangeAccountId).shouldBeEmpty()
        }

        test("TC-89596a4c-e81a-4ba2-a36b-769f4b46a277 연도 경계를 넘는 표시 대상 기간은 두 연도의 기록을 함께 담는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2027, 1, 20), amount = 700)
            record(accountId, routine, date = LocalDate(2026, 12, 20), amount = 500)

            dateAmountList(accountId, start = LocalDate(2026, 12, 13), endInclusive = LocalDate(2027, 1, 23)) shouldBe
                listOf(DateAmount(LocalDate(2026, 12, 20), 500), DateAmount(LocalDate(2027, 1, 20), 700))
        }

        test("TC-b3a7b970-8f04-4210-9cd1-f855dae82318 TC-7cdd0d5f-43ef-4791-8afc-6c09ecda6e34 여러 달에 걸친 표시 대상 기간도 한 번의 조회로 그 기간의 기록을 모두 담는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            listOf(
                LocalDate(2026, 4, 30),
                LocalDate(2026, 5, 1),
                LocalDate(2026, 6, 15),
                LocalDate(2026, 7, 15),
                LocalDate(2026, 8, 15),
                LocalDate(2026, 9, 30),
                LocalDate(2026, 10, 1),
            ).shuffled().forEach { date -> record(accountId, routine, date = date, amount = 500) }

            dateAmountList(accountId, start = LocalDate(2026, 5, 1), endInclusive = LocalDate(2026, 9, 30)).map { dateAmount -> dateAmount.date } shouldBe
                listOf(LocalDate(2026, 5, 1), LocalDate(2026, 6, 15), LocalDate(2026, 7, 15), LocalDate(2026, 8, 15), LocalDate(2026, 9, 30))
        }

        test("TC-071ec21f-07e0-4498-ad64-2307436abbd1 루틴이 삭제되면 그 루틴의 캘린더 루틴 기록이 모두 빠지고 삭제를 되돌리면 삭제되지 않은 기록의 합계가 다시 담긴다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 300, isDeleted = true)
            record(accountId, routine, date = LocalDate(2026, 7, 12), amount = 200)
            val transaction = AccountRoutineTransactionImpl(database = database)

            dateAmountFlow(accountId).test {
                awaitItem() shouldBe listOf(july(10, 500), july(12, 200))

                transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                awaitItem().shouldBeEmpty()

                transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 500), july(12, 200))

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-28157de2-515c-425f-bc52-c07a677656e5 기록이 삭제되면 그 날짜의 합계에서 빠지고 남은 기록이 없는 날짜는 결과에서 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            val july10Record = record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 300)
            val july12Record = record(accountId, routine, date = LocalDate(2026, 7, 12), amount = 200)
            val transaction = AccountRoutineRecordTransactionImpl(database = database)

            dateAmountFlow(accountId).test {
                awaitItem() shouldBe listOf(july(10, 800), july(12, 200))

                transaction.updateDeleted(accountId = accountId, recordId = july10Record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 500), july(12, 200))

                transaction.updateDeleted(accountId = accountId, recordId = july10Record.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 800), july(12, 200))

                transaction.updateDeleted(accountId = accountId, recordId = july12Record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                awaitItem() shouldBe listOf(july(10, 800))

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-4d1f4d13-24f5-4c02-8661-71c817e7de5e TC-d29ed8b7-48ec-4cbe-a826-39436334a96d 태그 연결은 캘린더 루틴 기록의 표시 색, 날짜, 합계와 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertWater(accountId)
            val tagColor = Arb.long().filter { color -> color != routine.detail.color }.next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next().let { tag -> tag.copy(detail = tag.detail.copy(color = tagColor)) }
            database.insertRoutineTestTag(accountId, tag)
            record(accountId, routine, date = LocalDate(2026, 7, 10), amount = 500)
            record(accountId, routine, date = LocalDate(2026, 7, 12), amount = 200)
            val before = database.calendarRoutineRecordList(accountId)
            before.map { calendarRecord -> calendarRecord.color } shouldBe listOf(routine.detail.color, routine.detail.color)

            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id)
            database.calendarRoutineRecordList(accountId) shouldBe before

            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            database.calendarRoutineRecordList(accountId) shouldBe before
        }
    })
