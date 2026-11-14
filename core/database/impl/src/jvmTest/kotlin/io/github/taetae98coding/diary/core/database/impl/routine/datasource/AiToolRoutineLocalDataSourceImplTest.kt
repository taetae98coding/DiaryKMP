package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource.AccountRoutineRecordLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class AiToolRoutineLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database = routineTestDatabase()
        }

        afterTest {
            database.close()
        }

        test("TC-68c32f9f-68c5-4b5c-8ac5-74e6e6bff138 루틴 하나의 이번 주기의 합계는 오늘 날짜의 삭제되지 않은 기록만 더한다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 3, 20)
            listOf(
                listOf(1000, 500) to 1500L,
                listOf(2500) to 2500L,
                emptyList<Int>() to 0L,
            ).forEach { (todayAmountList, expected) ->
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycleLocalEntity.DAILY, goalAmount = 2000).next()
                database.insertRoutine(accountId, routine)
                todayAmountList.forEach { amount -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = amount) }
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 19), amount = 300)
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 100, isDeleted = true)

                database.findSummary(accountId = accountId, routineId = routine.id, today = today) shouldBe RoutineSummaryLocalEntity(routine = routine, cycleAmount = expected)
            }
        }

        test("루틴 하나의 이번 주기의 합계는 매주 루틴이면 그 주, 매월 루틴이면 그 달의 기록을 더한다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 3, 20)
            val recordList = listOf(LocalDate(2026, 3, 1) to 1, LocalDate(2026, 3, 14) to 2, LocalDate(2026, 3, 15) to 4, LocalDate(2026, 3, 21) to 8, LocalDate(2026, 3, 22) to 16, LocalDate(2026, 4, 1) to 32)
            listOf(
                RoutineCycleLocalEntity.WEEKLY to 12L,
                RoutineCycleLocalEntity.MONTHLY to 31L,
            ).forEach { (cycle, expected) ->
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), cycle = cycle).next()
                database.insertRoutine(accountId, routine)
                recordList.forEach { (date, amount) -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = date, amount = amount) }

                database.findSummary(accountId = accountId, routineId = routine.id, today = today)?.cycleAmount shouldBe expected
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 루틴은 삭제된 상태로 찾고, 다른 계정의 루틴과 없는 루틴은 찾지 못한다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 3, 20)
            val deleted = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), isDeleted = true).next()
            val otherAccountRoutine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            database.insertRoutine(accountId, deleted)
            database.insertRoutine(Arb.kotlinUuid().next(), otherAccountRoutine)

            database.findSummary(accountId = accountId, routineId = deleted.id, today = today)?.routine?.isDeleted shouldBe true
            database.findSummary(accountId = accountId, routineId = otherAccountRoutine.id, today = today) shouldBe null
            database.findSummary(accountId = accountId, routineId = Arb.kotlinUuid().next(), today = today) shouldBe null
        }

        test("TC-31b4b63f-09f6-48e4-9f73-6d2c095a695f 기록 하나는 삭제 여부와 함께 찾고, 다른 계정의 기록과 없는 기록은 찾지 못한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            val otherAccountRoutine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            database.insertRoutine(accountId, routine)
            database.insertRoutine(otherAccountId, otherAccountRoutine)
            val saved = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 20), amount = 250)
            val removed = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 20), amount = 100, isDeleted = true)
            val otherAccountRecord = database.insertRoutineRecord(accountId = otherAccountId, routineId = otherAccountRoutine.id, date = LocalDate(2026, 3, 20), amount = 500)
            val dataSource = AccountRoutineRecordLocalDataSourceImpl(database = database)

            dataSource.find(accountId = accountId, recordId = saved.id).first() shouldBe saved
            dataSource.find(accountId = accountId, recordId = removed.id).first() shouldBe removed
            dataSource.find(accountId = accountId, recordId = otherAccountRecord.id).first() shouldBe null
            dataSource.find(accountId = accountId, recordId = Arb.kotlinUuid().next()).first() shouldBe null
        }
    })

private suspend fun DiaryDatabase.findSummary(
    accountId: Uuid,
    routineId: Uuid,
    today: LocalDate,
): RoutineSummaryLocalEntity? =
    AccountRoutineLocalDataSourceImpl(database = this)
        .findSummary(accountId = accountId, routineId = routineId, currentCycle = routineCurrentCycleOf(today = today))
        .first()
