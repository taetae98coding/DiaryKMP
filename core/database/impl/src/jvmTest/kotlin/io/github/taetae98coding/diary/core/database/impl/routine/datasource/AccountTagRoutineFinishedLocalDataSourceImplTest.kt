package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.selectRoutineFilterTag
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagRoutineFinishedLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagRoutineLocalDataSourceImpl
        lateinit var transaction: AccountRoutineTransactionImpl

        beforeTest {
            database = routineTestDatabase()
            dataSource = AccountTagRoutineLocalDataSourceImpl(database = database)
            transaction = AccountRoutineTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        fun tag(): TagLocalEntity = Arb.localTag(isFinished = false, isDeleted = false).next()

        suspend fun insertLinked(
            accountId: Uuid,
            tagId: Uuid,
            vararg routineList: RoutineLocalEntity,
        ) {
            database.insertRoutine(accountId, *routineList)
            routineList.forEach { routine -> database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tagId) }
        }

        suspend fun finishedTitles(
            accountId: Uuid,
            tagId: Uuid,
            today: LocalDate = TODAY,
        ): List<String> =
            dataSource
                .pageFinished(accountId = accountId, tagId = tagId, currentCycle = routineCurrentCycleOf(today = today))
                .loadAll()
                .map { summary -> summary.routine.detail.title }

        suspend fun tabTitles(
            accountId: Uuid,
            tagId: Uuid,
        ): List<String> =
            dataSource
                .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, currentCycle = routineCurrentCycleOf(today = TODAY))
                .loadAll()
                .map { summary -> summary.routine.detail.title }

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
            tagId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = dataSource.pageFinished(accountId = accountId, tagId = tagId, currentCycle = routineCurrentCycleOf(today = TODAY))
            pagingSource.loadAll()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

        suspend fun updateFinished(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            isFinished: Boolean,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): Int = transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = isFinished, updatedAt = updatedAt)

        suspend fun updateDeleted(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            isDeleted: Boolean,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): Int = transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = isDeleted, updatedAt = updatedAt)

        test("TC-64ebc156-aca1-4218-8732-c1d63422252c TC-e1da8a40-2a4e-49d3-ae21-a6cd9b54288c 현재 계정의 대상 태그와 활성 연결을 가진 완료·미삭제 루틴만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val otherTag = tag()
            database.insertRoutineTestTag(accountId, targetTag, otherTag)
            database.insertRoutineTestTag(otherAccountId, targetTag)
            val unlinked = routine(title = "e-unlinked")
            insertLinked(
                accountId,
                targetTag.id,
                routine(title = "a-finished"),
                routine(title = "b-unfinished", isFinished = false),
                routine(title = "c-deleted", isDeleted = true),
                unlinked,
            )
            database.linkRoutineTag(accountId = accountId, routineId = unlinked.id, tagId = targetTag.id, isDeleted = true)
            insertLinked(accountId, otherTag.id, routine(title = "d-other-tag"))
            insertLinked(otherAccountId, targetTag.id, routine(title = "f-other-account"))

            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("a-finished")
        }

        test("TC-399f5872-303f-4d26-bc8a-55e979ed1158 태그의 완료·삭제 상태는 연결된 완료 루틴의 노출에 영향을 주지 않는다") {
            listOf(true to false, false to true).forEach { (isFinished, isDeleted) ->
                val accountId = Arb.kotlinUuid().next()
                val targetTag = tag()
                database.insertRoutineTestTag(accountId, targetTag)
                insertLinked(accountId, targetTag.id, routine(title = "Alpha"))

                database.insertRoutineTestTag(accountId, targetTag.copy(isFinished = isFinished, isDeleted = isDeleted))

                finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("Alpha")
            }
        }

        test("TC-a790ed25-431f-4048-8ab6-c7c2c5245389 대상 태그에서 이어지는 태그에만 연결된 완료 루틴은 조회하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val tagB = tag()
            database.insertRoutineTestTag(accountId, targetTag, tagB)
            AccountTagLinkTransactionImpl(database = database).upsert(
                accountId = accountId,
                fromTagId = targetTag.id,
                toTagId = tagB.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
            insertLinked(accountId, tagB.id, routine(title = "Alpha"))

            finishedTitles(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
            finishedTitles(accountId = accountId, tagId = tagB.id) shouldBe listOf("Alpha")
        }

        test("TC-029a2f63-ef6d-4422-8e32-73ffdc679e61 RoutineHome의 태그 필터 선택은 이 목록에 적용하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val otherTag = tag()
            database.insertRoutineTestTag(accountId, targetTag, otherTag)
            insertLinked(accountId, targetTag.id, routine(title = "Alpha"))

            database.selectRoutineFilterTag(accountId = accountId, tagId = otherTag.id)

            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("Alpha")
        }

        test("목록은 진행 중, 시작 전 순으로 놓고 같은 상태 안에서는 제목 오름차순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            database.insertRoutineTestTag(accountId, targetTag)
            insertLinked(
                accountId,
                targetTag.id,
                routine(title = "가", startDate = LocalDate(2026, 3, 25)),
                routine(title = "나", startDate = LocalDate(2026, 3, 21)),
                routine(title = "다", startDate = LocalDate(2026, 3, 20)),
                routine(title = "라", startDate = LocalDate(2026, 1, 1)),
            )

            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("다", "라", "가", "나")
            finishedTitles(accountId = accountId, tagId = targetTag.id, today = LocalDate(2026, 3, 21)) shouldBe listOf("나", "다", "라", "가")
        }

        test("TC-53b1e84d-e7b5-4b71-a486-0abc17333e5b 다시 시작한 루틴을 다시 완료하면 순서 기준에 맞는 자리에 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val water = routine(title = "물 마시기", startDate = LocalDate(2026, 3, 1))
            val plant = routine(title = "물 주기", startDate = LocalDate(2026, 3, 1))
            database.insertRoutineTestTag(accountId, targetTag)
            insertLinked(accountId, targetTag.id, plant, water)
            updateFinished(accountId = accountId, routine = water, isFinished = false) shouldBe 1
            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 주기")

            updateFinished(accountId = accountId, routine = water, isFinished = true) shouldBe 1

            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기", "물 주기")
        }

        test("TC-9179153b-7a39-4d3f-a2fd-4e0844c1278a 완료된 루틴의 제목이나 시작 날짜를 고치면 바뀐 값의 자리로 옮긴다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val water = routine(title = "물 마시기", startDate = LocalDate(2026, 3, 1))
            val plant = routine(title = "물 주기", startDate = LocalDate(2026, 3, 1))
            database.insertRoutineTestTag(accountId, targetTag)
            insertLinked(accountId, targetTag.id, water, plant)
            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기", "물 주기")

            transaction.updateDetail(accountId = accountId, routineId = water.id, detail = water.detail.copy(title = "물 한 잔"), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 주기", "물 한 잔")

            transaction.updateDetail(accountId = accountId, routineId = water.id, detail = water.detail.copy(startDate = LocalDate(2026, 3, 21)), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            finishedTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 주기", "물 마시기")
        }

        test("TC-2743eed2-f3df-4f93-a5c0-d7247c24bb43 TC-c9993c05-2702-47b9-a87f-fa2a85ca7c93 다시 시작과 삭제, 실행 취소는 루틴과 태그 연결, 대상 태그를 남기고 상태와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            database.insertRoutineTestTag(accountId, targetTag)
            val routine = routine(title = "Alpha")
            insertLinked(accountId, targetTag.id, routine)
            val routineDataSource = AccountRoutineLocalDataSourceImpl(database = database)
            val tagDataSource = AccountTagLocalDataSourceImpl(database = database)
            val changeList: List<Pair<suspend (Instant) -> Int, RoutineLocalEntity>> =
                listOf(
                    Pair({ updatedAt -> updateFinished(accountId, routine, isFinished = false, updatedAt = updatedAt) }, routine.copy(isFinished = false)),
                    Pair({ updatedAt -> updateFinished(accountId, routine, isFinished = true, updatedAt = updatedAt) }, routine),
                    Pair({ updatedAt -> updateDeleted(accountId, routine, isDeleted = true, updatedAt = updatedAt) }, routine.copy(isDeleted = true)),
                    Pair({ updatedAt -> updateDeleted(accountId, routine, isDeleted = false, updatedAt = updatedAt) }, routine),
                )

            changeList.forEachIndexed { index, (change, expected) ->
                val updatedAt = routine.updatedAt + (index + 1).seconds

                change(updatedAt) shouldBe 1

                routineDataSource.find(accountId = accountId, routineId = routine.id).first() shouldBe expected.copy(updatedAt = updatedAt)
                linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(targetTag.id)
                tagDataSource.find(accountId = accountId, tagId = targetTag.id).first() shouldBe targetTag
            }
        }

        test("TC-17773b79-0ac5-40dd-8f88-89a44d67e4c4 다시 시작한 루틴은 루틴 탭 목록에 나타나고 삭제한 루틴은 두 목록 모두에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            database.insertRoutineTestTag(accountId, targetTag)
            val restarted = routine(title = "Alpha")
            val deleted = routine(title = "Bravo")
            insertLinked(accountId, targetTag.id, restarted, deleted)

            updateFinished(accountId, restarted, isFinished = false) shouldBe 1
            updateDeleted(accountId, deleted, isDeleted = true) shouldBe 1

            finishedTitles(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
            tabTitles(accountId = accountId, tagId = targetTag.id) shouldBe listOf("Alpha")
        }

        test("TC-d58b031f-38c3-4cf2-8352-964b3d2abf1e 루틴이나 연결이 바뀌면 열어 둔 목록이 갱신된다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            database.insertRoutineTestTag(accountId, targetTag)
            val water = routine(title = "물 마시기", startDate = LocalDate(2026, 3, 1))
            val plant = routine(title = "물 주기", startDate = LocalDate(2026, 3, 1))
            val boil = routine(title = "물 끓이기", startDate = LocalDate(2026, 3, 1), isFinished = false)
            insertLinked(accountId, targetTag.id, water, plant, boil)

            suspend fun titles(): List<String> = finishedTitles(accountId = accountId, tagId = targetTag.id)

            suspend fun change(block: suspend () -> Unit) = assertOpenedPageInvalidated(accountId = accountId, tagId = targetTag.id, change = block)

            suspend fun updateDetail(routine: RoutineLocalEntity) {
                transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = routine.detail, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            }
            titles() shouldBe listOf("물 마시기", "물 주기")

            change { updateFinished(accountId, water, isFinished = false) shouldBe 1 }
            titles() shouldBe listOf("물 주기")
            change { updateFinished(accountId, water, isFinished = true) shouldBe 1 }

            change { updateDeleted(accountId, water, isDeleted = true) shouldBe 1 }
            titles() shouldBe listOf("물 주기")
            change { updateDeleted(accountId, water, isDeleted = false) shouldBe 1 }

            change { updateFinished(accountId, boil, isFinished = true) shouldBe 1 }
            titles() shouldBe listOf("물 끓이기", "물 마시기", "물 주기")
            change { updateFinished(accountId, boil, isFinished = false) shouldBe 1 }

            change { database.linkRoutineTag(accountId = accountId, routineId = water.id, tagId = targetTag.id, isDeleted = true) }
            titles() shouldBe listOf("물 주기")
            change { database.linkRoutineTag(accountId = accountId, routineId = water.id, tagId = targetTag.id) }
            titles() shouldBe listOf("물 마시기", "물 주기")

            change { updateDetail(water.copy(detail = water.detail.copy(title = "물 한 잔"))) }
            titles() shouldBe listOf("물 주기", "물 한 잔")

            change { updateDetail(water.copy(detail = water.detail.copy(startDate = LocalDate(2026, 3, 21)))) }
            titles() shouldBe listOf("물 주기", "물 마시기")
        }
    }) {
    private companion object {
        const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
        val TODAY: LocalDate = LocalDate(2026, 3, 20)

        // 따로 정하지 않으면 오늘 2026-03-20에 진행 중인 완료된 루틴이다.
        fun routine(
            title: String,
            startDate: LocalDate = LocalDate(2026, 1, 1),
            isFinished: Boolean = true,
            isDeleted: Boolean = false,
        ): RoutineLocalEntity = Arb.localRoutine(startDate = startDate, title = title, isFinished = isFinished, isDeleted = isDeleted).next()
    }
}
