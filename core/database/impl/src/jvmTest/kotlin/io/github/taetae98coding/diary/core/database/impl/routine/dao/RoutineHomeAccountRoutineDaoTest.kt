package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.findAccountRoutineList
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.markRoutineUploaded
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineCycleListDoneOn
import io.github.taetae98coding.diary.core.database.impl.routine.routineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.routineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.selectRoutineFilterTag
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.core.testing.tag.localTag
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
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RoutineHomeAccountRoutineDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database = routineTestDatabase()
        }

        afterTest {
            database.close()
        }

        suspend fun pagedIds(accountId: Uuid): List<Uuid> = database.routineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

        suspend fun calendarRoutineIds(accountId: Uuid): List<Uuid> = database.calendarRoutineRecordList(accountId = accountId).map { calendarRecord -> calendarRecord.routineId }

        suspend fun selectedTagIdList(accountId: Uuid): List<Uuid> =
            AccountRoutineFilterLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        suspend fun find(
            accountId: Uuid,
            routineId: Uuid,
        ): RoutineLocalEntity? = AccountRoutineLocalDataSourceImpl(database = database).find(accountId = accountId, routineId = routineId).first()

        // 오늘이 속한 달에 기록 하나를 가진 진행 중인 루틴을 저장한다.
        suspend fun insertRecorded(
            accountId: Uuid,
            title: String,
            isDeleted: Boolean = false,
        ): RoutineLocalEntity {
            val routine = routine(title = title, isDeleted = isDeleted)
            database.insertRoutine(accountId, routine)
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = 1)

            return routine
        }

        // 목록 화면은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 새로 요청하지 않아도 반영된다는 것을 무효화로 확인한다.
        suspend fun assertOpenedPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = AccountRoutineLocalDataSourceImpl(database = database).page(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
            pagingSource.loadAll()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

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
            routineId: Uuid,
            isFinished: Boolean,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): Int = AccountRoutineTransactionImpl(database = database).updateFinished(accountId = accountId, routineId = routineId, isFinished = isFinished, updatedAt = updatedAt)

        suspend fun updateDeleted(
            accountId: Uuid,
            routineId: Uuid,
            isDeleted: Boolean,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): Int = AccountRoutineTransactionImpl(database = database).updateDeleted(accountId = accountId, routineId = routineId, isDeleted = isDeleted, updatedAt = updatedAt)

        test("TC-ddf146fd-79f8-40e3-ae8e-70d353a32cbf 현재 계정과 연결되고 완료되지도 삭제되지도 않은 루틴을 모두 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val notStarted = routine(title = "Alpha", startDate = LocalDate(2026, 8, 1))
            val notDoneToday = routine(title = "Hotel", cycle = RoutineCycleLocalEntity.WEEKENDS)
            val noGoal = routine(title = "Charlie")
            val achieved = routine(title = "Delta", goalAmount = 3)
            val noRecord = routine(title = "Echo", goalAmount = 3)
            database.insertRoutine(
                accountId,
                notStarted,
                notDoneToday,
                noGoal,
                achieved,
                noRecord,
                routine(title = "Bravo", isFinished = true),
                routine(title = "Bravo2", startDate = LocalDate(2026, 8, 1), isFinished = true),
                routine(title = "Foxtrot", isDeleted = true),
            )
            database.insertRoutine(otherAccountId, routine(title = "Golf"))
            database.insertRoutineRecord(accountId = accountId, routineId = achieved.id, date = TODAY, amount = 3)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(notStarted.id, notDoneToday.id, noGoal.id, achieved.id, noRecord.id)
        }

        test("TC-925b05f1-650b-4a66-8cf6-d6d4dda30c20 목록은 진행 중, 시작 전 순으로 놓고 같은 상태 안에서는 제목 오름차순으로 놓는다") {
            val accountId = Arb.kotlinUuid().next()
            val na = routine(title = "나", startDate = LocalDate(2026, 7, 1), cycle = RoutineCycleLocalEntity.WEEKENDS, unit = "", goalAmount = 10)
            val ra = routine(title = "라", startDate = LocalDate(2026, 7, 15), cycle = RoutineCycleLocalEntity.WEEKLY, unit = "m")
            database.insertRoutine(
                accountId,
                routine(title = "가", startDate = LocalDate(2026, 7, 16), cycle = RoutineCycleLocalEntity.WEEKDAYS, unit = "쪽"),
                na,
                ra,
                routine(title = "마", startDate = LocalDate(2026, 8, 1), cycle = RoutineCycleLocalEntity.DAILY),
            )
            database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = TODAY, amount = 1)
            database.insertRoutineRecord(accountId = accountId, routineId = ra.id, date = TODAY, amount = 9000)

            database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("나", "라", "가", "마")
        }

        test("TC-180d8c6d-c98a-4e86-8fec-4523943fb45b 현재 계정 기준으로 기기에 저장된 루틴을 나누어 불러온다") {
            val accountId = Arb.kotlinUuid().next()
            val routineList = listOf("Alpha", "Bravo", "Charlie", "Delta", "Echo").map { title -> routine(title = title) }
            database.insertRoutine(accountId, *routineList.shuffled().toTypedArray())
            database.insertRoutine(Arb.kotlinUuid().next(), routine(title = "Alpha2"))
            val dataSource = AccountRoutineLocalDataSourceImpl(database = database)

            val first =
                dataSource
                    .page(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()
            val second =
                dataSource
                    .page(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY))
                    .load(PagingSource.LoadParams.Append(key = first.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()

            first.data.map { summary -> summary.routine.id } shouldBe routineList.take(2).map { routine -> routine.id }
            second.data.map { summary -> summary.routine.id } shouldBe routineList.drop(2).take(2).map { routine -> routine.id }
        }

        test("TC-180d8c6d-c98a-4e86-8fec-4523943fb45b 보기 범위 기준을 만족하는 루틴만 나누어 불러온다") {
            val accountId = Arb.kotlinUuid().next()
            val todayList = listOf("Alpha", "Charlie", "Echo").map { title -> routine(title = title) }
            database.insertRoutine(
                accountId,
                *todayList.toTypedArray(),
                routine(title = "Bravo", cycle = RoutineCycleLocalEntity.WEEKENDS),
                routine(title = "Delta", startDate = LocalDate(2026, 7, 16)),
            )
            val dataSource = AccountRoutineLocalDataSourceImpl(database = database)

            fun pagingSource() = dataSource.pageStarted(accountId = accountId, currentCycle = routineCurrentCycleOf(today = TODAY), cycleList = routineCycleListDoneOn(today = TODAY))

            val first =
                pagingSource()
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()
            val second =
                pagingSource()
                    .load(PagingSource.LoadParams.Append(key = first.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, RoutineSummaryLocalEntity>>()

            first.data.map { summary -> summary.routine.id } shouldBe todayList.take(2).map { routine -> routine.id }
            second.data.map { summary -> summary.routine.id } shouldBe todayList.drop(2).map { routine -> routine.id }
        }

        test("TC-2d115de0-7a00-44de-85a0-f8fe82507656 TC-62e580b1-8520-4fb5-bf67-3498742ac101 여러 태그를 선택하면 하나 이상과 연결된 루틴을 다시 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val tagB = Arb.localTag(isFinished = false, isDeleted = false).next()
            val aRoutine = routine(title = "Bravo")
            val bRoutine = routine(title = "Alpha")
            val bothRoutine = routine(title = "Charlie")
            val noTagRoutine = routine(title = "Delta")
            database.insertRoutine(accountId, aRoutine, bRoutine, bothRoutine, noTagRoutine)
            database.insertRoutineTestTag(accountId, tagA, tagB)
            database.linkRoutineTag(accountId = accountId, routineId = aRoutine.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = bRoutine.id, tagId = tagB.id)
            database.linkRoutineTag(accountId = accountId, routineId = bothRoutine.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = bothRoutine.id, tagId = tagB.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(aRoutine.id, bothRoutine.id)

            assertOpenedPageInvalidated(accountId) { database.selectRoutineFilterTag(accountId = accountId, tagId = tagB.id) }

            pagedIds(accountId) shouldBe listOf(bRoutine.id, aRoutine.id, bothRoutine.id)
        }

        test("TC-f7966cbb-26e2-41e3-b445-8eff6c9a02b7 태그 필터는 목록 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val ga = routine(title = "가")
            val na = routine(title = "나")
            val da = routine(title = "다")
            val ra = routine(title = "라", startDate = LocalDate(2026, 8, 1))
            val ma = routine(title = "마", startDate = LocalDate(2026, 8, 2))
            database.insertRoutine(accountId, ma, ra, da, na, ga)
            database.insertRoutineTestTag(accountId, tagA)
            listOf(ma, ga, ra, da).forEach { routine -> database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id) }
            database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "나", "다", "라", "마")

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "다", "라", "마")
        }

        test("TC-3f8ddfc4-ac23-49c7-bd80-9c7052ad0a1d 추가한 루틴이 적용한 필터에 맞지 않으면 조회되지 않고 선택을 해제하면 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val aRoutine = routine(title = "Alpha")
            database.insertRoutine(accountId, aRoutine)
            database.insertRoutineTestTag(accountId, tagA)
            database.linkRoutineTag(accountId = accountId, routineId = aRoutine.id, tagId = tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            val addedRoutine = routine(title = "Bravo")

            AccountRoutineTransactionImpl(database = database).upsert(accountId = accountId, routine = addedRoutine, routineTagList = emptyList())

            pagedIds(accountId) shouldBe listOf(aRoutine.id)

            database.routineFilterTagDao().delete(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(aRoutine.id, addedRoutine.id)
        }

        test("TC-e7e0d005-b730-4324-8b3d-dbea1efc6320 태그 필터는 해제되지 않은 연결로만 판정한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val routineX = insertRecorded(accountId, title = "X")
            val routineY = insertRecorded(accountId, title = "Y")
            database.linkRoutineTag(accountId = accountId, routineId = routineX.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = routineY.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = routineY.id, tagId = tagA.id, isDeleted = true)

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(routineX.id)
            calendarRoutineIds(accountId) shouldBe listOf(routineX.id)
        }

        test("TC-677c3c59-a128-4993-8098-882ba48bc8ba 태그와 태그의 연결을 따라 판정하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val tagB = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA, tagB)
            val routine = insertRecorded(accountId, title = "Alpha")
            database.tagLinkDao().upsert(
                TagLinkLocalEntity(
                    fromTagId = tagA.id,
                    toTagId = tagB.id,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
            )
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagB.id)

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId).shouldBeEmpty()
            calendarRoutineIds(accountId).shouldBeEmpty()
        }

        test("TC-4f4abd15-fd12-455c-9ee8-ad0e114f2856 선택한 태그와 연결되어 있어도 삭제된 루틴과 그 기록은 표시하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val active = insertRecorded(accountId, title = "Alpha")
            val deleted = insertRecorded(accountId, title = "Bravo", isDeleted = true)
            database.linkRoutineTag(accountId = accountId, routineId = active.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = deleted.id, tagId = tagA.id)

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(active.id)
            calendarRoutineIds(accountId) shouldBe listOf(active.id)
        }

        test("TC-d3133150-9803-45c5-a0a9-0885c9a0eb31 달력의 태그 필터는 루틴 단위로 판정한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val routineX = routine(title = "X")
            val routineY = routine(title = "Y")
            database.insertRoutine(accountId, routineX, routineY)
            listOf(routineX, routineY).forEach { routine ->
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 7, 10), amount = 1)
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 7, 20), amount = 1)
            }
            database.linkRoutineTag(accountId = accountId, routineId = routineX.id, tagId = tagA.id)

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            database.calendarRoutineRecordList(accountId = accountId).map { calendarRecord -> calendarRecord.routineId to calendarRecord.date } shouldBe
                listOf(routineX.id to LocalDate(2026, 7, 10), routineX.id to LocalDate(2026, 7, 20))
        }

        context("TC-c899736a-783d-4e51-8a6e-62b9da997222 선택한 태그를 선택할 수 없게 되면 그 선택을 무시한다") {
            data class Case(
                val name: String,
                val makeUnselectable: suspend (accountId: Uuid, tagId: Uuid) -> Unit,
            )

            listOf(
                Case("완료") { accountId, tagId ->
                    database.accountTagDao().updateFinished(accountId = accountId, tagId = tagId, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
                },
                Case("삭제") { accountId, tagId ->
                    database.accountTagDao().updateDeleted(accountId = accountId, tagId = tagId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
                },
                Case("현재 사용자 계정과의 연결 해제") { accountId, _ ->
                    database.accountDataDao().deleteAccountTag(accountId = accountId)
                },
            ).forEach { case ->
                test(case.name) {
                    val accountId = Arb.kotlinUuid().next()
                    val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
                    database.insertRoutineTestTag(accountId, tagA)
                    val tagRoutine = insertRecorded(accountId, title = "Alpha")
                    val noTagRoutine = insertRecorded(accountId, title = "Bravo")
                    database.linkRoutineTag(accountId = accountId, routineId = tagRoutine.id, tagId = tagA.id)
                    database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
                    pagedIds(accountId) shouldBe listOf(tagRoutine.id)

                    assertOpenedPageInvalidated(accountId) { case.makeUnselectable(accountId, tagA.id) }

                    pagedIds(accountId) shouldBe listOf(tagRoutine.id, noTagRoutine.id)
                    calendarRoutineIds(accountId) shouldBe listOf(tagRoutine.id, noTagRoutine.id)
                    selectedTagIdList(accountId).shouldBeEmpty()
                }
            }
        }

        test("TC-2319742e-fb58-48af-9315-9f6cd0e11e2f 태그가 다시 선택할 수 있게 되면 이전 선택을 다시 적용한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val tagRoutine = insertRecorded(accountId, title = "Alpha")
            val noTagRoutine = insertRecorded(accountId, title = "Bravo")
            database.linkRoutineTag(accountId = accountId, routineId = tagRoutine.id, tagId = tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            database.accountTagDao().updateFinished(accountId = accountId, tagId = tagA.id, isFinished = true, updatedAt = Instant.fromEpochMilliseconds(2_000))
            pagedIds(accountId) shouldBe listOf(tagRoutine.id, noTagRoutine.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountTagDao().updateFinished(accountId = accountId, tagId = tagA.id, isFinished = false, updatedAt = Instant.fromEpochMilliseconds(3_000))
            }

            pagedIds(accountId) shouldBe listOf(tagRoutine.id)
            calendarRoutineIds(accountId) shouldBe listOf(tagRoutine.id)
            selectedTagIdList(accountId) shouldBe listOf(tagA.id)
        }

        test("TC-71ba9e96-d9ad-428e-8dc3-5c9f3c13411f 무시되고 있는 선택만 남으면 보이는 선택이 없고 삭제가 풀리면 그 선택이 다시 적용된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = true).next()
            database.insertRoutineTestTag(accountId, tagA)
            val tagRoutine = insertRecorded(accountId, title = "Alpha")
            val noTagRoutine = insertRecorded(accountId, title = "Bravo")
            database.linkRoutineTag(accountId = accountId, routineId = tagRoutine.id, tagId = tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            selectedTagIdList(accountId).shouldBeEmpty()
            pagedIds(accountId) shouldBe listOf(tagRoutine.id, noTagRoutine.id)

            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tagA.copy(isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(tagA.updatedAt.toEpochMilliseconds() + 1_000))),
                cursor = Arb.long().next(),
            )

            selectedTagIdList(accountId) shouldBe listOf(tagA.id)
            pagedIds(accountId) shouldBe listOf(tagRoutine.id)
        }

        test("TC-3237c371-33cd-4e46-8960-62101549ac42 필터를 적용한 동안 태그 연결이 바뀌면 다시 고르지 않아도 목록과 달력에서 사라지거나 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val routineX = insertRecorded(accountId, title = "Bravo")
            val routineY = insertRecorded(accountId, title = "Alpha")
            val other = insertRecorded(accountId, title = "Charlie")
            database.linkRoutineTag(accountId = accountId, routineId = routineX.id, tagId = tagA.id)
            database.linkRoutineTag(accountId = accountId, routineId = other.id, tagId = tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(routineX.id, other.id)
            calendarRoutineIds(accountId) shouldBe listOf(routineX.id, other.id)

            assertOpenedPageInvalidated(accountId) { database.linkRoutineTag(accountId = accountId, routineId = routineX.id, tagId = tagA.id, isDeleted = true) }
            assertOpenedPageInvalidated(accountId) { database.linkRoutineTag(accountId = accountId, routineId = routineY.id, tagId = tagA.id) }

            pagedIds(accountId) shouldBe listOf(routineY.id, other.id)
            calendarRoutineIds(accountId) shouldBe listOf(routineY.id, other.id)
        }

        context("TC-5b2ecc6d-c194-4aef-875b-26c8dd7f69ee 저장 상태가 바뀌면 열어 둔 목록이 노출, 필터와 정렬 기준으로 갱신된다") {
            test("노출 기준을 만족하는 루틴을 추가하면 목록 정렬에 맞는 자리에 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val first = routine(title = "가")
                val last = routine(title = "다")
                database.insertRoutine(accountId, first, last)
                val added = routine(title = "나")

                assertOpenedPageInvalidated(accountId) { database.insertRoutine(accountId, added) }

                pagedIds(accountId) shouldBe listOf(first.id, added.id, last.id)
            }

            test("삭제 상태로 바뀌면 사라지고 미삭제로 되돌리면 목록 정렬에 맞는 자리에 다시 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val routineList = listOf("가", "나", "다").map { title -> routine(title = title) }
                database.insertRoutine(accountId, *routineList.toTypedArray())

                assertOpenedPageInvalidated(accountId) { updateDeleted(accountId = accountId, routineId = routineList[1].id, isDeleted = true) shouldBe 1 }
                pagedIds(accountId) shouldBe listOf(routineList[0].id, routineList[2].id)

                assertOpenedPageInvalidated(accountId) { updateDeleted(accountId = accountId, routineId = routineList[1].id, isDeleted = false) shouldBe 1 }
                pagedIds(accountId) shouldBe routineList.map { routine -> routine.id }
            }

            test("제목과 시작 날짜가 수정되면 바뀐 값으로 목록 정렬에 맞는 자리로 옮겨진다") {
                val accountId = Arb.kotlinUuid().next()
                val ga = routine(title = "가")
                val na = routine(title = "나")
                val da = routine(title = "다")
                database.insertRoutine(accountId, ga, na, da)

                assertOpenedPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(title = "하"))) }
                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "다", "하")

                assertOpenedPageInvalidated(accountId) { updateDetail(accountId, ga.copy(detail = ga.detail.copy(startDate = LocalDate(2026, 7, 16)))) }
                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("다", "하", "가")
            }

            test("컬러, 단위, 목표량, 주기, 설명과 한 번에 기록할 양이 수정되면 바뀐 값과 합계로 조회되고 자리는 그대로다") {
                val accountId = Arb.kotlinUuid().next()
                val ga = routine(title = "가")
                val na = routine(title = "나", unit = "ml", goalAmount = 2000)
                val da = routine(title = "다")
                database.insertRoutine(accountId, ga, na, da)
                database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = TODAY, amount = 1500)
                database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = LocalDate(2026, 7, 1), amount = 700)
                val changed =
                    na.copy(
                        detail =
                            na.detail.copy(
                                description = "${na.detail.description}-changed",
                                color = na.detail.color + 1,
                                cycle = RoutineCycleLocalEntity.MONTHLY,
                                unit = "count",
                                goalAmount = null,
                                amountPerRecord = na.detail.amountPerRecord % 9_999_999 + 1,
                            ),
                    )
                database.routineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.cycleAmount } shouldBe listOf(0L, 1500L, 0L)

                assertOpenedPageInvalidated(accountId) { updateDetail(accountId, changed) }

                val summaryList = database.routineSummaryList(accountId = accountId, today = TODAY)
                summaryList.map { summary -> summary.routine.id } shouldBe listOf(ga.id, na.id, da.id)
                summaryList[1].routine.detail shouldBe changed.detail
                summaryList[1].cycleAmount shouldBe 2200L
            }

            test("단위가 비워지면 합계와 목표량은 그대로이고 빈 단위로 조회된다") {
                val accountId = Arb.kotlinUuid().next()
                val na = routine(title = "나", unit = "ml", goalAmount = 2000)
                database.insertRoutine(accountId, routine(title = "가"), na, routine(title = "다"))
                database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = TODAY, amount = 1500)

                assertOpenedPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(unit = ""))) }

                val summary = database.routineSummaryList(accountId = accountId, today = TODAY)[1]
                summary.routine.id shouldBe na.id
                summary.routine.detail.unit shouldBe ""
                summary.routine.detail.goalAmount shouldBe 2000
                summary.cycleAmount shouldBe 1500L
            }

            listOf(RoutineCycleLocalEntity.WEEKDAYS, RoutineCycleLocalEntity.WEEKENDS).forEach { cycle ->
                test("주기가 ${cycle}로 수정되면 오늘의 합계와 자리는 그대로다") {
                    val accountId = Arb.kotlinUuid().next()
                    val na = routine(title = "나", unit = "ml", goalAmount = 2000)
                    database.insertRoutine(accountId, routine(title = "가"), na, routine(title = "다"))
                    database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = TODAY, amount = 1500)
                    database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = LocalDate(2026, 7, 14), amount = 700)

                    assertOpenedPageInvalidated(accountId) { updateDetail(accountId, na.copy(detail = na.detail.copy(cycle = cycle))) }

                    val summaryList = database.routineSummaryList(accountId = accountId, today = TODAY)
                    summaryList.map { summary -> summary.routine.detail.title } shouldBe listOf("가", "나", "다")
                    summaryList[1].routine.detail.cycle shouldBe cycle
                    summaryList[1].cycleAmount shouldBe 1500L
                }
            }

            test("서버에서 내려받은 루틴이 반영되면 노출 기준과 정렬 기준에 따라 목록에 반영된다") {
                val accountId = Arb.kotlinUuid().next()
                val first = routine(title = "Alpha")
                val last = routine(title = "Charlie")
                database.insertRoutine(accountId, first, last)
                database.markRoutineUploaded(accountId = accountId)
                val pulled = routine(title = "Bravo")
                val pulledDeleted = routine(title = "Bravo2", isDeleted = true)
                val renamedLast = last.copy(detail = last.detail.copy(title = "0-Charlie"), updatedAt = Instant.fromEpochMilliseconds(last.updatedAt.toEpochMilliseconds() + 1_000))

                assertOpenedPageInvalidated(accountId) {
                    AccountRoutineSyncTransactionImpl(database = database).upsert(
                        accountId = accountId,
                        routineList = listOf(pulled, pulledDeleted, renamedLast),
                        cursor = Arb.long().next(),
                    )
                }

                pagedIds(accountId) shouldBe listOf(last.id, first.id, pulled.id)
            }
        }

        context("TC-685d6a20-1a30-467f-bf3e-96382f57d24c 루틴 기록이 추가되거나 삭제 상태가 바뀌면 합계만 다시 정하고 순서는 그대로다") {
            suspend fun prepare(accountId: Uuid): List<RoutineLocalEntity> {
                val routineList =
                    listOf(
                        routine(title = "가"),
                        routine(title = "나", unit = "ml", goalAmount = 2000),
                        routine(title = "다"),
                    )
                database.insertRoutine(accountId, *routineList.toTypedArray())
                database.insertRoutineRecord(accountId = accountId, routineId = routineList[1].id, date = TODAY, amount = 1000)

                return routineList
            }

            suspend fun cycleAmountList(accountId: Uuid): List<Pair<String, Long>> = database.routineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.detail.title to summary.cycleAmount }

            test("오늘 날짜에 양 500인 기록을 넣었다가 빼면 합계가 1500이 되었다가 1000으로 돌아온다") {
                val accountId = Arb.kotlinUuid().next()
                val na = prepare(accountId)[1]
                val transaction = AccountRoutineRecordTransactionImpl(database = database)
                val record = Arb.localRoutineRecord(routineId = na.id, date = TODAY, amount = 500).next()

                assertOpenedPageInvalidated(accountId) { transaction.upsert(accountId = accountId, record = record) shouldBe 1 }
                cycleAmountList(accountId) shouldBe listOf("가" to 0L, "나" to 1500L, "다" to 0L)

                assertOpenedPageInvalidated(accountId) {
                    transaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                }
                cycleAmountList(accountId) shouldBe listOf("가" to 0L, "나" to 1000L, "다" to 0L)
            }

            test("서버에서 내려받은 오늘 날짜의 양 250인 기록이 반영되면 합계가 1250이 된다") {
                val accountId = Arb.kotlinUuid().next()
                val na = prepare(accountId)[1]

                assertOpenedPageInvalidated(accountId) {
                    AccountRoutineRecordSyncTransactionImpl(database = database).upsert(
                        accountId = accountId,
                        recordList = listOf(Arb.localRoutineRecord(routineId = na.id, date = TODAY, amount = 250).next()),
                        cursor = Arb.long().next(),
                    )
                }

                cycleAmountList(accountId) shouldBe listOf("가" to 0L, "나" to 1250L, "다" to 0L)
            }

            test("어제 날짜에 양 500인 기록이 추가되어도 매일 루틴의 합계는 그대로다") {
                val accountId = Arb.kotlinUuid().next()
                val na = prepare(accountId)[1]

                AccountRoutineRecordTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    record = Arb.localRoutineRecord(routineId = na.id, date = LocalDate(2026, 7, 14), amount = 500).next(),
                ) shouldBe 1

                cycleAmountList(accountId) shouldBe listOf("가" to 0L, "나" to 1000L, "다" to 0L)
            }
        }

        test("TC-1336b40c-0a9f-43f3-be56-23a72bb05de3 TC-5585c876-466c-4887-8d63-9c54b0c6a7d5 TC-091631e1-6514-4f40-b33a-87d5eeb743a4 삭제와 실행 취소는 현재 계정 루틴의 삭제 여부와 수정 시각만 바꾸고 업로드 대기로 기록한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routine = routine(title = "Alpha")
            val otherAccountRoutine = routine(title = "Bravo")
            database.insertRoutine(accountId, routine)
            database.insertRoutine(otherAccountId, otherAccountRoutine)
            val recordList = List(2) { index -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = index + 1) }
            database.markRoutineUploaded(accountId = accountId)
            database.markRoutineUploaded(accountId = otherAccountId)
            val syncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            val deletedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 1_000)
            val restoredAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 2_000)

            updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = deletedAt) shouldBe 1
            updateDeleted(accountId = accountId, routineId = otherAccountRoutine.id, isDeleted = true, updatedAt = deletedAt) shouldBe 0

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isDeleted = true, updatedAt = deletedAt)
            find(accountId = otherAccountId, routineId = otherAccountRoutine.id) shouldBe otherAccountRoutine
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            pagedIds(accountId).shouldBeEmpty()
            syncDataSource.readPendingList(accountId = accountId).map { pending -> pending.id } shouldBe listOf(routine.id)
            syncDataSource.readPendingList(accountId = otherAccountId).shouldBeEmpty()

            database.markRoutineUploaded(accountId = accountId)
            updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = false, updatedAt = restoredAt) shouldBe 1

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isDeleted = false, updatedAt = restoredAt)
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            pagedIds(accountId) shouldBe listOf(routine.id)
            syncDataSource.readPendingList(accountId = accountId).map { pending -> pending.id } shouldBe listOf(routine.id)
            database.findAccountRoutineList() shouldContainExactlyInAnyOrder
                listOf(
                    AccountRoutineLocalEntity(accountId = accountId, routineId = routine.id, isDirty = true),
                    AccountRoutineLocalEntity(accountId = otherAccountId, routineId = otherAccountRoutine.id, isDirty = false),
                )
        }

        test("TC-83014771-8024-42d5-bdd3-0313fa7c869a 루틴의 완료 여부는 달력의 표시 대상을 가르지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val unfinished = routine(title = "Alpha")
            val finished = routine(title = "Bravo", isFinished = true)
            val finishedDeleted = routine(title = "Charlie", isFinished = true, isDeleted = true)
            database.insertRoutine(accountId, unfinished, finished, finishedDeleted)
            listOf(unfinished, finished, finishedDeleted).forEach { routine ->
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 7, 10), amount = 1)
            }

            calendarRoutineIds(accountId) shouldContainExactlyInAnyOrder listOf(unfinished.id, finished.id)
        }

        test("TC-3d982bf4-5c1d-4827-beb4-dde375714f0f 선택한 태그와 연결된 완료된 루틴은 목록에 없고 기록은 달력에 있다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val x = routine(title = "X", isFinished = true)
            val y = routine(title = "Y", isFinished = true)
            database.insertRoutine(accountId, x, y)
            database.insertRoutineTestTag(accountId, tagA)
            database.linkRoutineTag(accountId = accountId, routineId = x.id, tagId = tagA.id)
            listOf(x, y).forEach { routine -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = 1) }

            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId).shouldBeEmpty()
            calendarRoutineIds(accountId) shouldBe listOf(x.id)
        }

        test("TC-1057a4b5-02cf-4544-b6cb-eff9f0375a24 완료나 삭제를 실행 취소한 순서는 목록 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val ga = routine(title = "가")
            val na = routine(title = "나")
            val da = routine(title = "다", startDate = LocalDate(2026, 8, 1))
            database.insertRoutine(accountId, ga, na, da)

            updateFinished(accountId = accountId, routineId = na.id, isFinished = true) shouldBe 1
            updateFinished(accountId = accountId, routineId = na.id, isFinished = false) shouldBe 1
            database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "나", "다")

            updateDeleted(accountId = accountId, routineId = ga.id, isDeleted = true) shouldBe 1
            updateDeleted(accountId = accountId, routineId = ga.id, isDeleted = false) shouldBe 1
            database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "나", "다")
        }

        context("TC-af7fae20-4fed-4520-93e3-d692c7778272 완료 여부가 바뀌면 열어 둔 목록에서 사라지거나 정렬에 맞는 자리에 나타난다") {
            suspend fun prepare(accountId: Uuid): List<RoutineLocalEntity> {
                val routineList = listOf("가", "나", "다").map { title -> routine(title = title) } + routine(title = "라", isFinished = true)
                database.insertRoutine(accountId, *routineList.toTypedArray())
                database.markRoutineUploaded(accountId = accountId)

                return routineList
            }

            fun RoutineLocalEntity.pulled(isFinished: Boolean): RoutineLocalEntity = copy(isFinished = isFinished, updatedAt = Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds() + 1_000))

            test("완료하면 사라진다") {
                val accountId = Arb.kotlinUuid().next()
                val na = prepare(accountId)[1]

                assertOpenedPageInvalidated(accountId) { updateFinished(accountId = accountId, routineId = na.id, isFinished = true) shouldBe 1 }

                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "다")
            }

            test("서버에서 내려받은 완료가 반영되면 사라진다") {
                val accountId = Arb.kotlinUuid().next()
                val na = prepare(accountId)[1]

                assertOpenedPageInvalidated(accountId) {
                    AccountRoutineSyncTransactionImpl(database = database).upsert(accountId = accountId, routineList = listOf(na.pulled(isFinished = true)), cursor = Arb.long().next())
                }

                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "다")
            }

            test("다시 시작하면 정렬에 맞는 자리에 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val ra = prepare(accountId)[3]

                assertOpenedPageInvalidated(accountId) { updateFinished(accountId = accountId, routineId = ra.id, isFinished = false) shouldBe 1 }

                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "나", "다", "라")
            }

            test("서버에서 내려받은 다시 시작이 반영되면 정렬에 맞는 자리에 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val ra = prepare(accountId)[3]

                assertOpenedPageInvalidated(accountId) {
                    AccountRoutineSyncTransactionImpl(database = database).upsert(accountId = accountId, routineList = listOf(ra.pulled(isFinished = false)), cursor = Arb.long().next())
                }

                database.routineTitleList(accountId = accountId, today = TODAY) shouldBe listOf("가", "나", "다", "라")
            }
        }

        test("TC-1f95e299-8738-4411-a9c0-7efec0662367 TC-44da985b-f646-490d-8f22-a744f33a8690 TC-6d3fa494-c938-45d6-9274-1bd2f4ec22d5 완료와 실행 취소는 완료 여부와 수정 시각만 바꾸고 업로드 대기로 기록한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val routine = routine(title = "Alpha")
            val otherAccountRoutine = routine(title = "Bravo")
            database.insertRoutineTestTag(accountId, tagA)
            database.insertRoutine(accountId, routine)
            database.insertRoutine(otherAccountId, otherAccountRoutine)
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id)
            val recordList = List(2) { index -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = index + 1) }
            database.markRoutineUploaded(accountId = accountId)
            database.markRoutineUploaded(accountId = otherAccountId)
            val syncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            val entityTagDataSource = AccountEntityTagLocalDataSourceImpl(database = database)
            val finishedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 1_000)
            val restartedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 2_000)

            suspend fun linkedTagIdList(): List<Uuid> = entityTagDataSource.getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id).first().map { tag -> tag.id }

            updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = finishedAt) shouldBe 1
            updateFinished(accountId = accountId, routineId = otherAccountRoutine.id, isFinished = true, updatedAt = finishedAt) shouldBe 0

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isFinished = true, updatedAt = finishedAt)
            find(accountId = otherAccountId, routineId = otherAccountRoutine.id) shouldBe otherAccountRoutine
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)
            syncDataSource.readPendingList(accountId = accountId).map { pending -> pending.id } shouldBe listOf(routine.id)
            syncDataSource.readPendingList(accountId = otherAccountId).shouldBeEmpty()

            database.markRoutineUploaded(accountId = accountId)
            updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = restartedAt) shouldBe 1

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isFinished = false, updatedAt = restartedAt)
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)
            pagedIds(accountId) shouldBe listOf(routine.id)
            syncDataSource.readPendingList(accountId = accountId).map { pending -> pending.id } shouldBe listOf(routine.id)
            database.findAccountRoutineList() shouldContainExactlyInAnyOrder
                listOf(
                    AccountRoutineLocalEntity(accountId = accountId, routineId = routine.id, isDirty = true),
                    AccountRoutineLocalEntity(accountId = otherAccountId, routineId = otherAccountRoutine.id, isDirty = false),
                )
        }

        test("TC-ce155f26-bce6-46d6-8019-89cca1f171e2 완료하고 실행 취소해도 루틴의 기록과 태그 연결은 그대로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val routine = routine(title = "Alpha")
            database.insertRoutine(accountId, routine, routine(title = "Bravo"))
            val recordList = List(2) { index -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = index + 1) }
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id)
            val entityTagDataSource = AccountEntityTagLocalDataSourceImpl(database = database)
            val finishedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 1_000)
            val restartedAt = Instant.fromEpochMilliseconds(routine.updatedAt.toEpochMilliseconds() + 2_000)

            suspend fun linkedTagIdList(): List<Uuid> = entityTagDataSource.getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id).first().map { tag -> tag.id }

            updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = finishedAt) shouldBe 1
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)

            updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = restartedAt) shouldBe 1
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(routine.id)
        }

        test("TC-b4940246-2db0-4693-8a2e-47f12aee0ad5 TC-919c4fef-14ad-4783-9a5a-f9118665d086 삭제하고 실행 취소해도 루틴의 기록과 태그 연결은 그대로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tagA)
            val routine = routine(title = "Alpha")
            database.insertRoutine(accountId, routine, routine(title = "Bravo"))
            val recordList = List(2) { index -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = index + 1) }
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagA.id)
            val entityTagDataSource = AccountEntityTagLocalDataSourceImpl(database = database)

            suspend fun linkedTagIdList(): List<Uuid> = entityTagDataSource.getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id).first().map { tag -> tag.id }

            updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true) shouldBe 1
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)

            updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = false) shouldBe 1
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            linkedTagIdList() shouldBe listOf(tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(routine.id)
            database.routineSummaryList(accountId = accountId, today = TODAY).single().cycleAmount shouldBe 3L
        }
    }) {
    private companion object {
        const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
        val TODAY: LocalDate = LocalDate(2026, 7, 15)

        // 따로 정하지 않으면 오늘 2026-07-15에 진행 중인 매일 루틴이다.
        fun routine(
            title: String,
            startDate: LocalDate = LocalDate(2026, 1, 1),
            cycle: RoutineCycleLocalEntity = RoutineCycleLocalEntity.DAILY,
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
