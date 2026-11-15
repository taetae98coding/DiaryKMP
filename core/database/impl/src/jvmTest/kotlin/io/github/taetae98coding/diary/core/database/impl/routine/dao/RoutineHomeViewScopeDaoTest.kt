package io.github.taetae98coding.diary.core.database.impl.routine.dao

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineCycleListDoneOn
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.routineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.selectRoutineFilterTag
import io.github.taetae98coding.diary.core.database.impl.routine.todayRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.todayRoutineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

// 보기 범위가 `오늘 할 루틴`일 때의 목록 조회와, 빈 상태를 가르는 완료되지 않은 루틴의 유무 조회를 확인한다.
class RoutineHomeViewScopeDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database = routineTestDatabase()
        }

        afterTest {
            database.close()
        }

        suspend fun todayTitles(
            accountId: Uuid,
            today: LocalDate = WEDNESDAY,
        ): List<String> = database.todayRoutineTitleList(accountId = accountId, today = today)

        suspend fun allTitles(
            accountId: Uuid,
            today: LocalDate = WEDNESDAY,
        ): List<String> = database.routineTitleList(accountId = accountId, today = today)

        // 오늘 할 루틴 조회에 넘기는 주기를 테스트가 직접 정한다.
        suspend fun startedTitles(
            accountId: Uuid,
            today: LocalDate,
            cycleList: List<RoutineCycleLocalEntity>,
        ): List<String> =
            AccountRoutineLocalDataSourceImpl(database = database)
                .pageStarted(accountId = accountId, currentCycle = routineCurrentCycleOf(today = today), cycleList = cycleList)
                .loadAll()
                .map { summary -> summary.routine.detail.title }

        suspend fun unfinishedExists(accountId: Uuid): Boolean = AccountRoutineLocalDataSourceImpl(database = database).getUnfinishedExists(accountId = accountId).first()

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

        suspend fun updateFinished(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            isFinished: Boolean,
        ) {
            AccountRoutineTransactionImpl(database = database).updateFinished(accountId = accountId, routineId = routine.id, isFinished = isFinished, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
        }

        suspend fun updateDeleted(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            isDeleted: Boolean,
        ) {
            AccountRoutineTransactionImpl(database = database).updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
        }

        // 서버에서 내려받은 루틴은 기기의 것보다 수정 시각이 뒤여야 반영된다.
        suspend fun pull(
            accountId: Uuid,
            routine: RoutineLocalEntity,
        ) {
            AccountRoutineSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                routineList = listOf(routine.copy(updatedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 1_000))),
                cursor = Arb.long().next(),
            )
        }

        // 목록 화면은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 다시 고르지 않아도 반영된다는 것을 무효화로 확인한다.
        suspend fun assertOpenedTodayPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource =
                AccountRoutineLocalDataSourceImpl(database = database)
                    .pageStarted(accountId = accountId, currentCycle = routineCurrentCycleOf(today = WEDNESDAY), cycleList = routineCycleListDoneOn(today = WEDNESDAY))
            pagingSource.loadAll()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

        test("TC-049a798b-fd5a-4713-bbe2-899bef51c34e 오늘 할 루틴만 조회하면 완료되지 않은 루틴 가운데 오늘 할 루틴만 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                *listOf(
                    routine(title = "가", cycle = RoutineCycleLocalEntity.DAILY),
                    routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKDAYS),
                    routine(title = "다", cycle = RoutineCycleLocalEntity.WEEKENDS),
                    routine(title = "라", cycle = RoutineCycleLocalEntity.WEEKLY),
                    routine(title = "마", cycle = RoutineCycleLocalEntity.MONTHLY, startDate = LocalDate(2026, 7, 15)),
                    routine(title = "바", cycle = RoutineCycleLocalEntity.DAILY, startDate = LocalDate(2026, 7, 16)),
                    routine(title = "사", cycle = RoutineCycleLocalEntity.WEEKDAYS, startDate = LocalDate(2026, 7, 16)),
                    routine(title = "아", cycle = RoutineCycleLocalEntity.DAILY, isFinished = true),
                ).shuffled().toTypedArray(),
            )

            todayTitles(accountId) shouldBe listOf("가", "나", "라", "마")
        }

        test("TC-63e99f28-7f79-45d0-8c89-ffa5f3e1ef10 완료된 루틴은 그 주기를 넘겨도 오늘 할 루틴 조회에 들지 않는다") {
            listOf(
                RoutineCycleLocalEntity.DAILY to LocalDate(2026, 11, 13),
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 11, 13),
                RoutineCycleLocalEntity.WEEKENDS to LocalDate(2026, 11, 14),
                RoutineCycleLocalEntity.WEEKLY to LocalDate(2026, 11, 13),
                RoutineCycleLocalEntity.MONTHLY to LocalDate(2026, 11, 13),
            ).forEach { (cycle, today) ->
                val accountId = Arb.kotlinUuid().next()
                database.insertRoutine(
                    accountId,
                    routine(title = "완료", cycle = cycle, startDate = LocalDate(2026, 11, 1), isFinished = true),
                    routine(title = "미완료", cycle = cycle, startDate = LocalDate(2026, 11, 1)),
                )

                startedTitles(accountId = accountId, today = today, cycleList = listOf(cycle)) shouldBe listOf("미완료")
            }
        }

        test("TC-1e4eafd2-87db-4b16-8363-6dc31e5901e4 시작 전인 루틴은 오늘 할 루틴 조회에 들지 않고 시작 날짜부터 든다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(accountId, routine(title = "가", cycle = RoutineCycleLocalEntity.DAILY, startDate = LocalDate(2026, 11, 13)))

            listOf(
                LocalDate(2026, 11, 12) to emptyList(),
                LocalDate(2026, 11, 13) to listOf("가"),
                LocalDate(2026, 11, 14) to listOf("가"),
            ).forEach { (today, expected) ->
                startedTitles(accountId = accountId, today = today, cycleList = RoutineCycleLocalEntity.entries) shouldBe expected
            }
        }

        test("TC-f62a1e31-cafd-459b-b99f-9bbd380f2a0a 기록, 목표량과 목표 달성 여부는 오늘 할 루틴 조회에 쓰지 않는다") {
            val friday = LocalDate(2026, 11, 13)
            val saturday = LocalDate(2026, 11, 14)

            // 금요일과 토요일에 하는 주기는 조회하는 쪽이 넘긴다.
            listOf(
                RecordCase(cycle = RoutineCycleLocalEntity.DAILY, today = friday, goalAmount = null, recordAmount = null, isQueried = true),
                RecordCase(cycle = RoutineCycleLocalEntity.DAILY, today = friday, goalAmount = 3, recordAmount = 3, isQueried = true),
                RecordCase(cycle = RoutineCycleLocalEntity.WEEKDAYS, today = friday, goalAmount = 3, recordAmount = 5, isQueried = true),
                RecordCase(cycle = RoutineCycleLocalEntity.WEEKDAYS, today = saturday, goalAmount = 3, recordAmount = null, isQueried = false),
                RecordCase(cycle = RoutineCycleLocalEntity.WEEKENDS, today = friday, goalAmount = null, recordAmount = 1, isQueried = false),
            ).forEach { case ->
                val accountId = Arb.kotlinUuid().next()
                val routine = routine(title = "가", cycle = case.cycle, startDate = LocalDate(2026, 11, 1), goalAmount = case.goalAmount)
                database.insertRoutine(accountId, routine)
                case.recordAmount?.let { amount -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = case.today, amount = amount) }
                val cycleList = if (case.today == friday) FRIDAY_CYCLE_LIST else SATURDAY_CYCLE_LIST

                startedTitles(accountId = accountId, today = case.today, cycleList = cycleList) shouldBe if (case.isQueried) listOf("가") else emptyList()
            }
        }

        test("TC-aa1f73aa-2d48-4a8e-a0f5-502d86cbc445 TC-04aa51df-0ce8-4ee5-a128-5c1e1cd657c6 평일만과 주말만 루틴은 그 주기를 넘긴 조회에만 든다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "평일만", cycle = RoutineCycleLocalEntity.WEEKDAYS, startDate = LocalDate(2026, 11, 1)),
                routine(title = "주말만", cycle = RoutineCycleLocalEntity.WEEKENDS, startDate = LocalDate(2026, 11, 1)),
            )

            startedTitles(accountId = accountId, today = LocalDate(2026, 11, 13), cycleList = FRIDAY_CYCLE_LIST) shouldBe listOf("평일만")
            startedTitles(accountId = accountId, today = LocalDate(2026, 11, 14), cycleList = SATURDAY_CYCLE_LIST) shouldBe listOf("주말만")
        }

        test("TC-ec997da8-0a75-4681-b7df-14198aacf409 TC-78d686ee-08a5-4885-9e15-967d2128d2f6 매일, 매주, 매월 루틴은 넘긴 주기에 들면 날짜와 관계없이 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "가", cycle = RoutineCycleLocalEntity.DAILY, startDate = LocalDate(2026, 11, 1)),
                routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKLY, startDate = LocalDate(2026, 11, 1)),
                routine(title = "다", cycle = RoutineCycleLocalEntity.MONTHLY, startDate = LocalDate(2026, 11, 1)),
            )

            // 조회는 요일과 공휴일을 보지 않고 넘겨받은 주기로만 고른다. 2026-12-25는 공휴일인 금요일이다.
            ((8..14).map { day -> LocalDate(2026, 11, day) } + LocalDate(2026, 12, 25)).forEach { today ->
                startedTitles(accountId = accountId, today = today, cycleList = FRIDAY_CYCLE_LIST) shouldBe listOf("가", "나", "다")
            }
        }

        test("TC-484e3e37-a791-40e0-a578-b47f5b5a4816 오늘의 요일에 따라 평일만과 주말만 루틴이 조회되거나 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "독서", cycle = RoutineCycleLocalEntity.WEEKDAYS),
                routine(title = "등산", cycle = RoutineCycleLocalEntity.WEEKENDS),
                routine(title = "운동", cycle = RoutineCycleLocalEntity.WEEKLY),
                routine(title = "저축", cycle = RoutineCycleLocalEntity.MONTHLY),
            )

            listOf(
                LocalDate(2026, 7, 10) to listOf("독서", "운동", "저축"),
                LocalDate(2026, 7, 11) to listOf("등산", "운동", "저축"),
                LocalDate(2026, 7, 12) to listOf("등산", "운동", "저축"),
                LocalDate(2026, 7, 13) to listOf("독서", "운동", "저축"),
            ).forEach { (today, expected) ->
                todayTitles(accountId, today = today) shouldBe expected
            }
        }

        test("TC-bb93d3d7-ef7b-40bf-9571-c9dae3bc536e 공휴일인 금요일에도 요일로만 판정해 평일만 루틴을 조회한다") {
            // 조회는 공휴일 정보를 입력으로 받지 않는다. 2026-07-17은 제헌절이고 금요일이다.
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "독서", cycle = RoutineCycleLocalEntity.WEEKDAYS),
                routine(title = "등산", cycle = RoutineCycleLocalEntity.WEEKENDS),
            )

            todayTitles(accountId, today = LocalDate(2026, 7, 17)) shouldBe listOf("독서")
        }

        test("TC-07fef83b-4cac-4e3a-8c90-ed97fcd1cf84 모든 루틴은 완료되지 않은 루틴을 모두, 오늘 할 루틴은 그 가운데 오늘 할 루틴만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "가"),
                routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKENDS),
                routine(title = "다", startDate = LocalDate(2026, 7, 16)),
                routine(title = "라", isFinished = true),
            )

            todayTitles(accountId) shouldBe listOf("가")
            allTitles(accountId) shouldBe listOf("가", "나", "다")
            todayTitles(accountId) shouldBe listOf("가")
        }

        test("TC-181573d3-b753-4a1f-b3d6-5e1962cf1c46 보기 범위는 목록 순서를 바꾸지 않고 오늘 할 루틴에서는 제목 오름차순으로만 놓인다") {
            val accountId = Arb.kotlinUuid().next()
            val saturday = LocalDate(2026, 7, 11)
            database.insertRoutine(
                accountId,
                routine(title = "마", cycle = RoutineCycleLocalEntity.DAILY, startDate = LocalDate(2026, 7, 12)),
                routine(title = "다", cycle = RoutineCycleLocalEntity.DAILY),
                routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKDAYS),
                routine(title = "라", cycle = RoutineCycleLocalEntity.WEEKENDS, startDate = LocalDate(2026, 7, 18)),
                routine(title = "가", cycle = RoutineCycleLocalEntity.WEEKENDS),
            )

            todayTitles(accountId, today = saturday) shouldBe listOf("가", "다")
            allTitles(accountId, today = saturday) shouldBe listOf("가", "나", "다", "라", "마")
        }

        test("TC-2272cec0-2dd9-4487-91cc-6502d3ca705d 보기 범위와 태그 필터를 함께 적용한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val linkedList =
                listOf(
                    routine(title = "가"),
                    routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKENDS),
                    routine(title = "다", startDate = LocalDate(2026, 7, 16)),
                )
            database.insertRoutine(accountId, *linkedList.toTypedArray(), routine(title = "라"))
            linkedList.forEach { routine -> database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id) }

            todayTitles(accountId) shouldBe listOf("가", "라")
            allTitles(accountId) shouldBe listOf("가", "나", "라", "다")

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            todayTitles(accountId) shouldBe listOf("가")
            allTitles(accountId) shouldBe listOf("가", "나", "다")
        }

        listOf(
            Triple("시작 날짜가 오늘보다 뒤", RoutineCycleLocalEntity.DAILY, LocalDate(2026, 7, 16)),
            Triple("오늘의 요일이 하는 요일이 아님", RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 7, 15)),
            Triple("하는 요일이지만 시작 날짜가 오늘보다 뒤", RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 7, 20)),
        ).forEach { (name, cycle, startDate) ->
            test("TC-d1a7a776-6f63-4832-8e3d-6e7cc0bf14f9 추가한 루틴이 오늘 할 루틴이 아니면 오늘 할 루틴 조회에는 없고 모든 루틴 조회에는 있다: $name") {
                val accountId = Arb.kotlinUuid().next()
                database.insertRoutine(accountId, routine(title = "가"))
                todayTitles(accountId) shouldBe listOf("가")

                database.insertRoutine(accountId, routine(title = "나", cycle = cycle, startDate = startDate))

                todayTitles(accountId) shouldBe listOf("가")
                allTitles(accountId).toSet() shouldBe setOf("가", "나")
            }
        }

        test("TC-be0a277f-164c-45df-b4b8-7d7ebec4217e 기록을 넣고 빼거나 오늘 목표를 채워도 오늘 할 루틴 조회에 그대로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = routine(title = "물 마시기", unit = "ml", goalAmount = 2000)
            database.insertRoutine(accountId, routine)
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = WEDNESDAY, amount = 1750)
            val recordTransaction = AccountRoutineRecordTransactionImpl(database = database)

            val added = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = WEDNESDAY, amount = 250)
            database.todayRoutineSummaryList(accountId = accountId, today = WEDNESDAY).map { summary -> summary.routine.id to summary.cycleAmount } shouldBe listOf(routine.id to 2000L)

            recordTransaction.updateDeleted(accountId = accountId, recordId = added.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            database.todayRoutineSummaryList(accountId = accountId, today = WEDNESDAY).map { summary -> summary.routine.id to summary.cycleAmount } shouldBe listOf(routine.id to 1750L)
        }

        test("TC-a747891d-2da5-46e2-8422-93afd70d71b9 평일만과 주말만 루틴의 합계는 매일 루틴처럼 오늘 날짜의 기록만 더한다") {
            listOf(
                Triple(RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 7, 10), LocalDate(2026, 7, 10) to 15) to 15L,
                Triple(RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 7, 13), LocalDate(2026, 7, 10) to 20) to 0L,
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 7, 11), LocalDate(2026, 7, 11) to 1) to 1L,
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 7, 12), LocalDate(2026, 7, 11) to 1) to 0L,
            ).forEach { (given, expected) ->
                val (cycle, today, record) = given
                val accountId = Arb.kotlinUuid().next()
                val routine = routine(title = "독서", cycle = cycle, startDate = LocalDate(2026, 7, 1))
                database.insertRoutine(accountId, routine)
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = record.first, amount = record.second)

                database.todayRoutineSummaryList(accountId = accountId, today = today).map { summary -> summary.cycleAmount } shouldBe listOf(expected)
            }
        }

        test("TC-92d3f5ec-33b9-44b6-8e1b-9bbc36e9cd4a 모든 루틴 조회에는 오늘의 요일이 하는 요일에 들지 않는 루틴도 오늘의 합계와 함께 조회된다") {
            listOf(
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 7, 11),
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 7, 12),
                RoutineCycleLocalEntity.WEEKENDS to LocalDate(2026, 7, 15),
                RoutineCycleLocalEntity.WEEKDAYS to LocalDate(2026, 7, 15),
            ).forEach { (cycle, today) ->
                val accountId = Arb.kotlinUuid().next()
                val routine = routine(title = "독서", cycle = cycle, startDate = LocalDate(2026, 7, 1), unit = "쪽", goalAmount = 20)
                database.insertRoutine(accountId, routine)

                AccountRoutineLocalDataSourceImpl(database = database)
                    .page(accountId = accountId, currentCycle = routineCurrentCycleOf(today = today))
                    .loadAll()
                    .map { summary -> summary.routine.id to summary.cycleAmount } shouldBe listOf(routine.id to 0L)
            }
        }

        test("TC-ada941d6-d2f9-4eaa-b5a4-d313a4615474 바뀐 오늘로 조회하면 그 오늘의 요일과 진행 상태로 조회되는 루틴이 바뀐다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "독서", cycle = RoutineCycleLocalEntity.WEEKDAYS, startDate = LocalDate(2026, 7, 1)),
                routine(title = "등산", cycle = RoutineCycleLocalEntity.WEEKENDS, startDate = LocalDate(2026, 7, 1)),
                routine(title = "요가", cycle = RoutineCycleLocalEntity.DAILY, startDate = LocalDate(2026, 7, 11)),
            )

            listOf(
                LocalDate(2026, 7, 10) to listOf("독서"),
                LocalDate(2026, 7, 11) to listOf("등산", "요가"),
                LocalDate(2026, 7, 12) to listOf("등산", "요가"),
                LocalDate(2026, 7, 13) to listOf("독서", "요가"),
            ).forEach { (today, expected) ->
                todayTitles(accountId, today = today) shouldBe expected
            }
        }

        context("TC-0455ffe0-672a-44a8-9fc1-71ff198a876d 시작 날짜나 주기가 바뀌면 다시 고르지 않아도 오늘 할 루틴 조회에서 사라지거나 나타난다") {
            lateinit var na: RoutineLocalEntity
            lateinit var da: RoutineLocalEntity
            lateinit var ra: RoutineLocalEntity
            val accountId = Arb.kotlinUuid().next()

            beforeTest {
                na = routine(title = "나", unit = "ml", goalAmount = 2000)
                da = routine(title = "다", startDate = LocalDate(2026, 7, 16))
                ra = routine(title = "라", cycle = RoutineCycleLocalEntity.WEEKENDS)
                database.insertRoutine(accountId, routine(title = "가"), na, da, ra)
                database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = WEDNESDAY, amount = 1500)
                todayTitles(accountId) shouldBe listOf("가", "나")
            }

            test("나의 시작 날짜를 2026-07-16으로 수정하면 사라진다") {
                assertOpenedTodayPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(startDate = LocalDate(2026, 7, 16)))) }

                todayTitles(accountId) shouldBe listOf("가")
            }

            test("나의 주기를 주말만으로 수정하면 사라진다") {
                assertOpenedTodayPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(cycle = RoutineCycleLocalEntity.WEEKENDS))) }

                todayTitles(accountId) shouldBe listOf("가")
            }

            test("서버에서 내려받은 나의 주기 주말만이 반영되면 사라진다") {
                assertOpenedTodayPageInvalidated(accountId) { pull(accountId, na.copy(detail = na.detail.copy(cycle = RoutineCycleLocalEntity.WEEKENDS))) }

                todayTitles(accountId) shouldBe listOf("가")
            }

            test("나의 주기가 평일만으로 수정되면 그대로 조회되고 합계도 그대로다") {
                assertOpenedTodayPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(cycle = RoutineCycleLocalEntity.WEEKDAYS))) }

                database.todayRoutineSummaryList(accountId = accountId, today = WEDNESDAY).map { summary -> summary.routine.detail.title to summary.cycleAmount } shouldBe
                    listOf("가" to 0L, "나" to 1500L)
            }

            test("다의 시작 날짜가 2026-07-15로 수정되면 나타난다") {
                assertOpenedTodayPageInvalidated(accountId) { updateDetail(accountId, da.copy(detail = da.detail.copy(startDate = LocalDate(2026, 7, 15)))) }

                todayTitles(accountId) shouldBe listOf("가", "나", "다")
            }

            listOf(RoutineCycleLocalEntity.DAILY, RoutineCycleLocalEntity.WEEKDAYS).forEach { cycle ->
                test("라의 주기가 ${cycle}로 수정되면 나타난다") {
                    assertOpenedTodayPageInvalidated(accountId) { updateDetail(accountId, ra.copy(detail = ra.detail.copy(cycle = cycle))) }

                    todayTitles(accountId) shouldBe listOf("가", "나", "라")
                }
            }
        }

        test("TC-f3044014-279c-4acc-881a-4ce519c30e33 다시 시작한 루틴은 보기 범위 기준에 맞을 때만 조회된다") {
            listOf(
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 1, 1), listOf("가")),
                Triple(RoutineCycleLocalEntity.DAILY, LocalDate(2026, 7, 16), listOf("가")),
                Triple(RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 1, 1), listOf("가", "나")),
            ).forEach { (cycle, startDate, expectedToday) ->
                val accountId = Arb.kotlinUuid().next()
                val finished = routine(title = "나", cycle = cycle, startDate = startDate, isFinished = true)
                database.insertRoutine(accountId, routine(title = "가"), finished)

                assertOpenedTodayPageInvalidated(accountId) { updateFinished(accountId, finished, isFinished = false) }

                todayTitles(accountId) shouldBe expectedToday
                allTitles(accountId) shouldBe listOf("가", "나")
            }
        }

        listOf("완료", "삭제").forEach { action ->
            listOf(
                "주기가 주말만으로 수정됨" to { routine: RoutineLocalEntity -> routine.copy(detail = routine.detail.copy(cycle = RoutineCycleLocalEntity.WEEKENDS)) },
                "시작 날짜가 2026-07-16으로 수정됨" to { routine: RoutineLocalEntity -> routine.copy(detail = routine.detail.copy(startDate = LocalDate(2026, 7, 16))) },
            ).forEach { (name, change) ->
                test("TC-2d2a748a-d6d0-4eeb-bf4f-c58412b9395c $action 뒤 ${name}을 거쳐 되돌린 루틴은 오늘 할 루틴 조회에는 없고 모든 루틴 조회에는 있다") {
                    val accountId = Arb.kotlinUuid().next()
                    val routine = routine(title = "가")
                    database.insertRoutine(accountId, routine)

                    suspend fun toggle(isOn: Boolean) = if (action == "완료") updateFinished(accountId, routine, isFinished = isOn) else updateDeleted(accountId, routine, isDeleted = isOn)

                    toggle(isOn = true)
                    updateDetail(accountId, change(routine))
                    toggle(isOn = false)

                    val restored = AccountRoutineLocalDataSourceImpl(database = database).find(accountId = accountId, routineId = routine.id).first()
                    restored?.isFinished shouldBe false
                    restored?.isDeleted shouldBe false
                    todayTitles(accountId).shouldBeEmpty()
                    allTitles(accountId) shouldBe listOf("가")
                }
            }
        }

        test("TC-24ebfe5d-b3a9-48c4-9f6e-c1d6b3d152d9 완료되지 않은 루틴은 있지만 오늘 할 루틴이 없으면 오늘 할 루틴 조회는 비고 완료되지 않은 루틴은 있다고 알린다") {
            listOf(
                listOf(routine(title = "가", startDate = LocalDate(2026, 7, 16))),
                listOf(routine(title = "가", cycle = RoutineCycleLocalEntity.WEEKENDS)),
                listOf(routine(title = "가", startDate = LocalDate(2026, 7, 16)), routine(title = "나", isFinished = true)),
            ).forEach { routineList ->
                val accountId = Arb.kotlinUuid().next()
                database.insertRoutine(accountId, *routineList.toTypedArray())

                todayTitles(accountId).shouldBeEmpty()
                unfinishedExists(accountId) shouldBe true
            }
        }

        test("TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 완료되지 않은 루틴의 유무는 완료된 루틴과 삭제된 루틴, 다른 계정의 루틴을 세지 않는다") {
            val emptyAccountId = Arb.kotlinUuid().next()
            val finishedOnlyAccountId = Arb.kotlinUuid().next()
            val notStartedAccountId = Arb.kotlinUuid().next()
            database.insertRoutine(finishedOnlyAccountId, routine(title = "가", isFinished = true), routine(title = "나", isDeleted = true), routine(title = "다", isFinished = true, isDeleted = true))
            database.insertRoutine(notStartedAccountId, routine(title = "가", startDate = LocalDate(2026, 7, 16)))

            unfinishedExists(emptyAccountId) shouldBe false
            unfinishedExists(finishedOnlyAccountId) shouldBe false
            unfinishedExists(notStartedAccountId) shouldBe true
        }

        test("완료되지 않은 루틴의 유무는 태그 필터 선택과 보기 범위 선택을 보지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tag)
            database.insertRoutine(accountId, routine(title = "가", cycle = RoutineCycleLocalEntity.WEEKENDS))
            database.selectRoutineFilterTag(accountId = accountId, tagId = tag.id)

            todayTitles(accountId).shouldBeEmpty()
            allTitles(accountId).shouldBeEmpty()
            unfinishedExists(accountId) shouldBe true
        }

        test("TC-68d686f2-29c1-4d2d-aefe-55fd3e84df8f 저장된 루틴이 바뀌면 완료되지 않은 루틴의 유무가 이어서 전달된다") {
            val accountId = Arb.kotlinUuid().next()
            val notStarted = routine(title = "가", startDate = LocalDate(2026, 7, 16))
            val pulled = routine(title = "나", cycle = RoutineCycleLocalEntity.WEEKENDS)

            AccountRoutineLocalDataSourceImpl(database = database).getUnfinishedExists(accountId = accountId).distinctUntilChanged().test {
                awaitItem() shouldBe false

                database.insertRoutine(accountId, notStarted)
                awaitItem() shouldBe true

                updateDeleted(accountId, notStarted, isDeleted = true)
                awaitItem() shouldBe false

                pull(accountId, pulled)
                awaitItem() shouldBe true

                updateFinished(accountId, pulled, isFinished = true)
                awaitItem() shouldBe false

                cancelAndIgnoreRemainingEvents()
            }
            todayTitles(accountId).shouldBeEmpty()
        }

        test("오늘 할 루틴 조회는 시작 날짜가 오늘과 같은 루틴부터 넣는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "어제", startDate = LocalDate(2026, 7, 14)),
                routine(title = "오늘", startDate = LocalDate(2026, 7, 15)),
                routine(title = "내일", startDate = LocalDate(2026, 7, 16)),
            )

            todayTitles(accountId) shouldBe listOf("어제", "오늘")
        }

        test("오늘 할 루틴 조회는 넘긴 주기의 루틴만 넣고 넘긴 주기가 없으면 비어 있다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(accountId, *RoutineCycleLocalEntity.entries.map { cycle -> routine(title = cycle.name, cycle = cycle) }.toTypedArray())
            val dataSource = AccountRoutineLocalDataSourceImpl(database = database)

            suspend fun titles(cycleList: List<RoutineCycleLocalEntity>): List<String> =
                dataSource
                    .pageStarted(accountId = accountId, currentCycle = routineCurrentCycleOf(today = WEDNESDAY), cycleList = cycleList)
                    .loadAll()
                    .map { summary -> summary.routine.detail.title }

            RoutineCycleLocalEntity.entries.forEach { cycle -> titles(listOf(cycle)) shouldBe listOf(cycle.name) }
            titles(emptyList()).shouldBeEmpty()
            titles(RoutineCycleLocalEntity.entries).toSet() shouldBe RoutineCycleLocalEntity.entries.map { cycle -> cycle.name }.toSet()
        }

        test("오늘 할 루틴 조회는 삭제된 루틴과 다른 계정의 루틴을 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(accountId, routine(title = "가"), routine(title = "나", isDeleted = true))
            database.insertRoutine(Arb.kotlinUuid().next(), routine(title = "다"))

            todayTitles(accountId) shouldBe listOf("가")
        }

        test("오늘 할 루틴 조회의 매주와 매월 루틴 합계는 이번 주와 이번 달의 기록을 더한다") {
            val accountId = Arb.kotlinUuid().next()
            val weekly = routine(title = "가", cycle = RoutineCycleLocalEntity.WEEKLY)
            val monthly = routine(title = "나", cycle = RoutineCycleLocalEntity.MONTHLY)
            database.insertRoutine(accountId, weekly, monthly)
            listOf(weekly, monthly).forEach { routine ->
                listOf(LocalDate(2026, 7, 1) to 1, LocalDate(2026, 7, 12) to 10, LocalDate(2026, 7, 15) to 100).forEach { (date, amount) ->
                    database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = date, amount = amount)
                }
            }

            database.todayRoutineSummaryList(accountId = accountId, today = WEDNESDAY).map { summary -> summary.cycleAmount } shouldBe listOf(110L, 111L)
        }

        test("보기 범위 선택을 저장해도 목록 조회가 스스로 좁혀지지는 않는다") {
            // 조회는 호출하는 쪽이 고른 보기 범위의 조회를 부르는 것으로만 갈린다.
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(accountId, routine(title = "가"), routine(title = "나", startDate = LocalDate(2026, 7, 16)))

            AccountRoutineFilterLocalDataSourceImpl(database = database).upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.TODAY)

            allTitles(accountId) shouldBe listOf("가", "나")
        }
    }) {
    private companion object {
        const val INVALIDATION_TIMEOUT_MILLIS = 30_000L

        val FRIDAY_CYCLE_LIST = listOf(RoutineCycleLocalEntity.DAILY, RoutineCycleLocalEntity.WEEKDAYS, RoutineCycleLocalEntity.WEEKLY, RoutineCycleLocalEntity.MONTHLY)
        val SATURDAY_CYCLE_LIST = listOf(RoutineCycleLocalEntity.DAILY, RoutineCycleLocalEntity.WEEKENDS, RoutineCycleLocalEntity.WEEKLY, RoutineCycleLocalEntity.MONTHLY)

        // 2026-07-15는 수요일이다.
        val WEDNESDAY: LocalDate = LocalDate(2026, 7, 15)

        // 따로 정하지 않으면 2026-07-15에 진행 중인 완료되지 않은 매일 루틴이다.
        fun routine(
            title: String,
            cycle: RoutineCycleLocalEntity = RoutineCycleLocalEntity.DAILY,
            startDate: LocalDate = LocalDate(2026, 1, 1),
            unit: String? = null,
            goalAmount: Int? = null,
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): RoutineLocalEntity =
            Arb
                .localRoutine(
                    startDate = startDate,
                    cycle = cycle,
                    title = title,
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    unit = unit,
                    goalAmount = goalAmount,
                ).next()
    }
}

private class RecordCase(
    val cycle: RoutineCycleLocalEntity,
    val today: LocalDate,
    val goalAmount: Int?,
    val recordAmount: Int?,
    val isQueried: Boolean,
)
