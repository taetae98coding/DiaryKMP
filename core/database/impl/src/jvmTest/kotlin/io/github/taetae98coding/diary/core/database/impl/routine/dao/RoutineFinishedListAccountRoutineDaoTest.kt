package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.finishedRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.finishedRoutineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.selectRoutineFilterTag
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RoutineFinishedListAccountRoutineDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountRoutineTransactionImpl

        beforeTest {
            database = routineTestDatabase()
            transaction = AccountRoutineTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun finishedIds(accountId: Uuid): List<Uuid> = database.finishedRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

        suspend fun finishedTitles(accountId: Uuid): List<String> = database.finishedRoutineTitleList(accountId = accountId, today = TODAY)

        suspend fun find(
            accountId: Uuid,
            routineId: Uuid,
        ): RoutineLocalEntity? = AccountRoutineLocalDataSourceImpl(database = database).find(accountId = accountId, routineId = routineId).first()

        suspend fun linkedTagIdList(
            accountId: Uuid,
            routineId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routineId)
                .first()
                .map { tag -> tag.id }

        // 목록 화면은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 새로 요청하지 않아도 반영된다는 것을 무효화로 확인한다.
        suspend fun assertOpenedPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = AccountRoutineLocalDataSourceImpl(database = database).pageFinished(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
            pagingSource.loadAll()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

        // 다시 시작, 삭제와 그 실행 취소를 이 순서로 실행하며, 각 동작 뒤의 완료 여부와 삭제 여부를 함께 담는다.
        fun stateChangeList(
            accountId: Uuid,
            routineId: Uuid,
        ): List<StateChange> =
            listOf(
                StateChange(isFinished = false, isDeleted = false) { updatedAt -> transaction.updateFinished(accountId = accountId, routineId = routineId, isFinished = false, updatedAt = updatedAt) },
                StateChange(isFinished = true, isDeleted = false) { updatedAt -> transaction.updateFinished(accountId = accountId, routineId = routineId, isFinished = true, updatedAt = updatedAt) },
                StateChange(isFinished = true, isDeleted = true) { updatedAt -> transaction.updateDeleted(accountId = accountId, routineId = routineId, isDeleted = true, updatedAt = updatedAt) },
                StateChange(isFinished = true, isDeleted = false) { updatedAt -> transaction.updateDeleted(accountId = accountId, routineId = routineId, isDeleted = false, updatedAt = updatedAt) },
            )

        test("TC-4fd3290d-187e-45c1-86d8-77dbc2c46f96 현재 계정의 완료·미삭제 루틴만 목록에서 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val finished = routine(title = "Alpha")
            database.insertRoutine(accountId, finished, routine(title = "Bravo", isFinished = false), routine(title = "Charlie", isDeleted = true))
            database.insertRoutine(otherAccountId, routine(title = "Delta"), routine(title = "Echo", isFinished = false), routine(title = "Foxtrot", isDeleted = true))

            finishedIds(accountId) shouldBe listOf(finished.id)
        }

        test("TC-db2ec998-a66e-4688-ae47-fd3cc7959b46 진행 상태, 목표량, 목표 달성과 기록의 유무는 노출 기준에 쓰지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val notStarted = routine(title = "Alpha", startDate = LocalDate(2026, 7, 16))
            val noGoal = routine(title = "Bravo", startDate = LocalDate(2026, 7, 1))
            val notAchieved = routine(title = "Charlie", startDate = LocalDate(2026, 7, 1), unit = "ml", goalAmount = 2000)
            val achieved = routine(title = "Delta", startDate = LocalDate(2026, 7, 1), unit = "ml", goalAmount = 2000)
            database.insertRoutine(accountId, notStarted, noGoal, notAchieved, achieved)
            database.insertRoutineRecord(accountId = accountId, routineId = notAchieved.id, date = TODAY, amount = 1500)
            database.insertRoutineRecord(accountId = accountId, routineId = achieved.id, date = TODAY, amount = 2000)

            database.finishedRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id to summary.cycleAmount } shouldContainExactlyInAnyOrder
                listOf(notStarted.id to 0L, noGoal.id to 0L, notAchieved.id to 1500L, achieved.id to 2000L)
        }

        test("TC-1e53ec42-f3b3-4f19-bdcf-126a65a0c53c 태그 필터 선택을 조회 조건에 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val linked = routine(title = "Alpha")
            val unlinked = routine(title = "Bravo")
            database.insertRoutineTestTag(accountId, tagA)
            database.insertRoutine(accountId, linked, unlinked)
            database.linkRoutineTag(accountId = accountId, routineId = linked.id, tagId = tagA.id)

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            finishedIds(accountId) shouldBe listOf(linked.id, unlinked.id)
        }

        RoutineViewScopeLocalEntity.entries.forEach { viewScope ->
            test("TC-3cd0a1e0-4f93-44bf-8d48-831b8831c050 보기 범위가 ${viewScope}여도 오늘의 요일과 관계없이 완료된 루틴을 모두 조회한다") {
                val accountId = Arb.kotlinUuid().next()
                database.insertRoutine(
                    accountId,
                    routine(title = "가"),
                    routine(title = "나").let { routine -> routine.copy(detail = routine.detail.copy(cycle = RoutineCycleLocalEntity.WEEKENDS)) },
                    routine(title = "다", startDate = LocalDate(2026, 7, 16)),
                )
                AccountRoutineFilterLocalDataSourceImpl(database = database).upsertViewScope(accountId = accountId, viewScope = viewScope)

                finishedTitles(accountId) shouldBe listOf("가", "나", "다")
            }
        }

        test("TC-72772dfc-3b49-45c8-b589-dc9aa8a90be1 목록은 진행 중, 시작 전 순으로 놓고 같은 상태 안에서는 제목 오름차순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "Charlie", startDate = LocalDate(2026, 7, 15)),
                routine(title = "Alpha", startDate = LocalDate(2026, 7, 16)),
                routine(title = "Bravo", startDate = LocalDate(2026, 7, 1)),
                routine(title = "Delta", startDate = LocalDate(2026, 8, 1)),
            )

            finishedTitles(accountId) shouldBe listOf("Bravo", "Charlie", "Alpha", "Delta")
        }

        test("TC-ed4c9a81-dbf7-460e-bfa7-7a6d1e9a1eb8 완료 시점은 목록 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routineList = listOf("Alpha", "Bravo", "Charlie").map { title -> routine(title = title, isFinished = false) }
            database.insertRoutine(accountId, *routineList.toTypedArray())

            routineList.reversed().forEachIndexed { index, routine ->
                transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = routine.updatedAt + (index + 1).seconds) shouldBe 1
            }

            finishedTitles(accountId) shouldBe listOf("Alpha", "Bravo", "Charlie")
        }

        test("TC-4df6ec01-4cb7-4c97-a909-eaf94a996459 완료한 시점은 완료된 루틴만 모은 목록의 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val ga = routine(title = "가", startDate = LocalDate(2026, 11, 1), isFinished = false)
            val na = routine(title = "나", startDate = LocalDate(2026, 11, 1), isFinished = false)
            val da = routine(title = "다", startDate = LocalDate(2026, 11, 20), isFinished = false)
            val ra = routine(title = "라", startDate = LocalDate(2026, 11, 20), isFinished = false)
            val ma = routine(title = "마", startDate = LocalDate(2026, 11, 20), isFinished = false)
            database.insertRoutine(accountId, ga, na, da, ra, ma)

            listOf(ma, na, da).forEachIndexed { index, routine ->
                transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = routine.updatedAt + (index + 1).seconds) shouldBe 1
            }

            database.finishedRoutineTitleList(accountId = accountId, today = today) shouldBe listOf("나", "다", "마")
        }

        test("TC-c07f8ee0-eefb-4858-91af-01ccb759f3e2 완료된 루틴 목록을 페이지 단위로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val routineList = listOf("Alpha", "Bravo", "Charlie", "Delta", "Echo").map { title -> routine(title = title) }
            database.insertRoutine(accountId, *routineList.shuffled().toTypedArray())
            val dataSource = AccountRoutineLocalDataSourceImpl(database = database)

            val first =
                dataSource
                    .pageFinished(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()
            val second =
                dataSource
                    .pageFinished(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
                    .load(PagingSource.LoadParams.Append(key = first.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()

            first.data.map { summary -> summary.routine.id } shouldBe routineList.take(2).map { routine -> routine.id }
            second.data.map { summary -> summary.routine.id } shouldBe routineList.drop(2).take(2).map { routine -> routine.id }
        }

        context("TC-b9ad17ed-593d-4641-87bc-2184ba10ceb6 저장된 루틴 변경을 열어 둔 목록에 반영한다") {
            test("완료 루틴을 다시 시작하거나 삭제하면 사라진다") {
                val accountId = Arb.kotlinUuid().next()
                val routineList = listOf("가", "나", "다").map { title -> routine(title = title) }
                database.insertRoutine(accountId, *routineList.toTypedArray())

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateFinished(accountId = accountId, routineId = routineList[0].id, isFinished = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("나", "다")

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateDeleted(accountId = accountId, routineId = routineList[1].id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("다")
            }

            test("미완료 루틴을 완료하거나 완료되고 삭제된 루틴의 삭제를 되돌리면 목록 순서에 맞는 자리에 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val unfinished = routine(title = "나", isFinished = false)
                val deleted = routine(title = "다", isDeleted = true)
                database.insertRoutine(accountId, routine(title = "가"), unfinished, deleted, routine(title = "라"))

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateFinished(accountId = accountId, routineId = unfinished.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("가", "나", "라")

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateDeleted(accountId = accountId, routineId = deleted.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("가", "나", "다", "라")
            }

            test("제목이나 시작 날짜가 바뀌면 바뀐 값으로 목록 순서에 맞는 자리로 옮겨진다") {
                val accountId = Arb.kotlinUuid().next()
                val ga = routine(title = "가")
                val na = routine(title = "나")
                database.insertRoutine(accountId, ga, na, routine(title = "다"))

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateDetail(accountId = accountId, routineId = na.id, detail = na.detail.copy(title = "하"), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("가", "다", "하")

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateDetail(
                        accountId = accountId,
                        routineId = ga.id,
                        detail = ga.detail.copy(startDate = LocalDate(2026, 7, 16)),
                        updatedAt = Arb.secondPrecisionInstant().next(),
                    ) shouldBe 1
                }
                finishedTitles(accountId) shouldBe listOf("다", "하", "가")
            }
        }

        test("TC-0ac61b76-833f-4f81-b9a6-95f2bf19cb22 TC-40677e3f-827f-4a47-8c62-97839e4b1410 TC-b4d82a29-6178-41a9-8cc5-72a10eb30be7 TC-e465b9b8-405c-4755-bc66-bfaef013cf4b TC-774c684c-1158-43f4-94d1-0cf7e4a7fb92 TC-2300a4b5-9cd2-499b-b005-33cfb830d42f 상태와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routine = routine(title = "Alpha")
            val otherAccountRoutine = routine(title = "Bravo")
            database.insertRoutine(accountId, routine)
            database.insertRoutine(otherAccountId, otherAccountRoutine)

            stateChangeList(accountId = accountId, routineId = routine.id).forEachIndexed { index, change ->
                val updatedAt = routine.updatedAt + (index + 1).seconds

                change.execute(updatedAt) shouldBe 1

                find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isFinished = change.isFinished, isDeleted = change.isDeleted, updatedAt = updatedAt)
            }
            stateChangeList(accountId = accountId, routineId = otherAccountRoutine.id).forEach { change ->
                change.execute(Arb.secondPrecisionInstant().next()) shouldBe 0
            }

            find(accountId = otherAccountId, routineId = otherAccountRoutine.id) shouldBe otherAccountRoutine
        }

        test("TC-acb87c90-dfb4-4bd7-b10e-8d39225a73a3 상태 변경은 루틴의 내용과 루틴 기록, 태그 연결을 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val routine = routine(title = "Alpha")
            database.insertRoutineTestTag(accountId, tagA)
            database.insertRoutine(accountId, routine)
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id)
            val recordList =
                listOf(false, true).map { isDeleted -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = 1, isDeleted = isDeleted) }

            stateChangeList(accountId = accountId, routineId = routine.id).forEach { change ->
                change.execute(Arb.secondPrecisionInstant().next()) shouldBe 1

                find(accountId = accountId, routineId = routine.id).shouldNotBeNull().detail shouldBe routine.detail
                database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
                linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tagA.id)
            }
        }
    }) {
    private class StateChange(
        val isFinished: Boolean,
        val isDeleted: Boolean,
        val execute: suspend (updatedAt: Instant) -> Int,
    )

    private companion object {
        const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
        val TODAY: LocalDate = LocalDate(2026, 7, 15)

        // 따로 정하지 않으면 오늘 2026-07-15에 진행 중인 완료된 매일 루틴이다.
        fun routine(
            title: String,
            startDate: LocalDate = LocalDate(2026, 1, 1),
            unit: String? = null,
            goalAmount: Int? = null,
            isFinished: Boolean = true,
            isDeleted: Boolean = false,
        ): RoutineLocalEntity =
            Arb
                .localRoutine(
                    startDate = startDate,
                    title = title,
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    unit = unit,
                    goalAmount = goalAmount,
                ).next()
    }
}
