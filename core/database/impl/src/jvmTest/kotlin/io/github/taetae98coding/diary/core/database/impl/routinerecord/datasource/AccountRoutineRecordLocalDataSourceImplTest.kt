package io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountRoutineRecordLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountRoutineRecordLocalDataSourceImpl

        beforeTest {
            database = routineTestDatabase()
            dataSource = AccountRoutineRecordLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun pagedList(
            accountId: Uuid,
            routineId: Uuid,
        ): List<RoutineRecordLocalEntity> = dataSource.page(accountId = accountId, routineId = routineId).loadAll()

        test("TC-36712683-5b5e-4204-a836-36ada41e3308 대상 루틴에 속하고 현재 계정과 연결된 삭제되지 않은 기록을 모두 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 10)).next()
            val otherRoutine = Arb.localRoutine(startDate = LocalDate(2026, 3, 10)).next()
            database.insertRoutine(accountId, routine, otherRoutine)
            val exposedList =
                listOf(LocalDate(2026, 3, 15), LocalDate(2026, 3, 25), LocalDate(2026, 3, 5), LocalDate(2026, 4, 5)).map { date ->
                    database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = date, amount = 1)
                }
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = 1, isDeleted = true)
            database.insertRoutineRecord(accountId = otherAccountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = 1)
            database.insertRoutineRecord(accountId = accountId, routineId = otherRoutine.id, date = LocalDate(2026, 3, 15), amount = 1)

            pagedList(accountId = accountId, routineId = routine.id) shouldContainExactlyInAnyOrder exposedList
        }

        test("TC-73cf82d6-c443-4699-9ec0-330e86816259 루틴의 완료 여부는 기록 내역의 노출 기준에 쓰지 않는다") {
            listOf(false, true).forEach { isFinished ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), isFinished = isFinished).next()
                database.insertRoutine(accountId, routine)
                val exposedList = List(2) { index -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = index + 1) }
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = 3, isDeleted = true)

                pagedList(accountId = accountId, routineId = routine.id) shouldContainExactlyInAnyOrder exposedList
            }
        }

        test("TC-a82d669a-5b25-4db9-b8e1-4f4888b8fe47 같은 날짜의 기록은 하나로 합치지 않고 기록마다 따로 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            database.insertRoutine(accountId, routine)
            listOf(250, 250, 500).forEach { amount -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = amount) }

            pagedList(accountId = accountId, routineId = routine.id).map { record -> record.amount } shouldContainExactlyInAnyOrder listOf(250, 250, 500)
        }

        test("TC-5ae95ccb-326f-463d-a238-bc435eddc83f 기록은 날짜가 늦은 순으로, 같은 날짜는 나중에 넣은 기록을 먼저 놓는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            database.insertRoutine(accountId, routine)
            val recordList =
                listOf(LocalDate(2026, 3, 10), LocalDate(2026, 3, 20), LocalDate(2026, 3, 10), LocalDate(2026, 3, 15)).mapIndexed { index, date ->
                    database.insertRoutineRecord(
                        accountId = accountId,
                        routineId = routine.id,
                        date = date,
                        amount = 1,
                        createdAt = Instant.fromEpochSeconds(1_000L + index),
                    )
                }

            pagedList(accountId = accountId, routineId = routine.id).map { record -> record.id } shouldBe listOf(1, 3, 2, 0).map { index -> recordList[index].id }
        }

        test("TC-fc7960b9-7463-4696-bd31-5afc50f49282 새 루틴은 기록을 하나도 갖지 않은 상태로 추가된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), goalAmount = 2000).next()

            AccountRoutineTransactionImpl(database = database).upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

            pagedList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
        }

        test("삭제된 루틴의 기록 내역은 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), isDeleted = true).next()
            database.insertRoutine(accountId, routine)
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = 1)

            pagedList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
        }

        test("기록 내역을 페이지 단위로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            database.insertRoutine(accountId, routine)
            val recordList = (1..5).map { day -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, day), amount = 1) }
            val expected = recordList.reversed().map { record -> record.id }

            val first =
                dataSource
                    .page(accountId = accountId, routineId = routine.id)
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineRecordLocalEntity>>()
            val second =
                dataSource
                    .page(accountId = accountId, routineId = routine.id)
                    .load(PagingSource.LoadParams.Append(key = first.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineRecordLocalEntity>>()

            first.data.map { record -> record.id } shouldBe expected.take(2)
            second.data.map { record -> record.id } shouldBe expected.drop(2).take(2)
        }

        // 기록 내역은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 새로 요청하지 않아도 반영된다는 것을 무효화로 확인한다.
        listOf<Pair<String, suspend (DiaryDatabase, Uuid, Uuid, RoutineRecordLocalEntity) -> Unit>>(
            "기록을 넣으면" to { database, accountId, routineId, _ ->
                AccountRoutineRecordTransactionImpl(database = database).upsert(accountId = accountId, record = Arb.localRoutineRecord(routineId = routineId).next())
            },
            "기록을 빼면" to { database, accountId, _, record ->
                AccountRoutineRecordTransactionImpl(database = database).updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            },
            "서버에서 내려받은 기록이 반영되면" to { database, accountId, routineId, _ ->
                AccountRoutineRecordSyncTransactionImpl(database = database).upsert(accountId = accountId, recordList = listOf(Arb.localRoutineRecord(routineId = routineId).next()), cursor = 1L)
            },
        ).forEach { (name, change) ->
            test("$name 열어 둔 기록 내역 조회가 무효화된다") {
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
                database.insertRoutine(accountId, routine)
                val record = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 15), amount = 1)
                val pagingSource = dataSource.page(accountId = accountId, routineId = routine.id)
                pagingSource.loadAll()
                val invalidated = CompletableDeferred<Unit>()
                pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

                change(database, accountId, routine.id, record)

                withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                pagingSource.invalid.shouldBeTrue()
            }
        }
    }) {
    private companion object {
        const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
    }
}
