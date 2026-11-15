package io.github.taetae98coding.diary.core.database.impl.routine

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountTagRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class RoutineDetailEntityTagTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        suspend fun upsertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        // 업로드를 마친 루틴을 저장한다.
        suspend fun insertRoutine(
            accountId: Uuid,
            isDeleted: Boolean = false,
            title: String? = null,
            isFinished: Boolean = false,
        ): RoutineLocalEntity {
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = title, isFinished = isFinished, isDeleted = isDeleted).next()
            AccountRoutineTransactionImpl(database = database).upsert(accountId = accountId, routine = routine, routineTagList = emptyList())
            val syncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            AccountRoutineSyncTransactionImpl(database = database).clearPending(accountId = accountId, routineList = syncDataSource.readPendingList(accountId = accountId))
            return routine
        }

        suspend fun link(
            accountId: Uuid,
            routineId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.ROUTINE,
                entityId = routineId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun find(
            accountId: Uuid,
            routineId: Uuid,
        ): RoutineLocalEntity? = AccountRoutineLocalDataSourceImpl(database = database).find(accountId = accountId, routineId = routineId).first()

        suspend fun displayedTagIdList(
            accountId: Uuid,
            routineId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routineId)
                .first()
                .map { tag -> tag.id }

        // 오늘 2026-07-15를 기준으로 모든 루틴이 진행 중이라 목록은 제목 오름차순이다.
        suspend fun homeIdList(accountId: Uuid): List<Uuid> = database.routineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

        suspend fun tagTabIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            AccountTagRoutineLocalDataSourceImpl(database = database)
                .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, currentCycle = routineCurrentCycleOf(today = TODAY))
                .loadAll()
                .map { summary -> summary.routine.id }

        listOf(
            Triple("미완료·미삭제", { tag: TagLocalEntity -> tag }, true),
            Triple("완료됨", { tag: TagLocalEntity -> tag.copy(isFinished = true) }, true),
            Triple("삭제됨", { tag: TagLocalEntity -> tag.copy(isDeleted = true) }, false),
        ).forEach { (name, change, isDisplayed) ->
            test("TC-12295682-a192-41d8-892e-6ba730796bee 연결된 태그가 $name 이면 태그 입력 표시 여부가 $isDisplayed 다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                upsertTag(accountId, tag)
                val routine = insertRoutine(accountId = accountId)
                link(accountId = accountId, routineId = routine.id, tagId = tag.id)

                upsertTag(accountId, change(tag))

                displayedTagIdList(accountId = accountId, routineId = routine.id) shouldBe if (isDisplayed) listOf(tag.id) else emptyList()
            }
        }

        test("TC-a7336fdd-2a1d-47c9-9eee-4802196f16bd TC-bde6b14d-4fd2-47b9-9ed0-1b870bdbfd06 태그 연결과 해제는 루틴의 정보, 삭제 여부와 수정 시각, 루틴 기록을 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tag)
            val routine = insertRoutine(accountId = accountId)
            val recordList =
                listOf(false, true).map { isDeleted ->
                    database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next(), isDeleted = isDeleted)
                }

            link(accountId = accountId, routineId = routine.id, tagId = tag.id)
            find(accountId = accountId, routineId = routine.id) shouldBe routine
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList

            link(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            find(accountId = accountId, routineId = routine.id) shouldBe routine
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
        }

        test("TC-f7b5855f-55f6-4924-8d6c-2edbeeeef43a 루틴을 완료하거나 다시 시작해도 태그 연결은 해제되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList = List(2) { Arb.localTag(isFinished = false, isDeleted = false).next() }
            upsertTag(accountId, *tagList.toTypedArray())
            val routine = insertRoutine(accountId = accountId)
            tagList.forEach { tag -> link(accountId = accountId, routineId = routine.id, tagId = tag.id) }
            val before = displayedTagIdList(accountId = accountId, routineId = routine.id)
            before shouldContainExactlyInAnyOrder tagList.map { tag -> tag.id }

            listOf(true, false).forEach { isFinished ->
                AccountRoutineTransactionImpl(database = database).updateFinished(
                    accountId = accountId,
                    routineId = routine.id,
                    isFinished = isFinished,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                ) shouldBe 1

                displayedTagIdList(accountId = accountId, routineId = routine.id) shouldBe before
            }
        }

        test("TC-871918ea-6aa8-4a5a-9cac-7178634cd5fe 태그 연결은 완료된 루틴 목록의 노출과 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tag)
            val routineList = listOf("가", "나", "다").map { title -> insertRoutine(accountId = accountId, title = title, isFinished = true) }
            val before = database.finishedRoutineSummaryList(accountId = accountId, today = TODAY)
            before.map { summary -> summary.routine.id } shouldBe routineList.map { routine -> routine.id }

            link(accountId = accountId, routineId = routineList[1].id, tagId = tag.id)

            database.finishedRoutineSummaryList(accountId = accountId, today = TODAY) shouldBe before
        }

        test("TC-7dbb69f2-0bd4-4ef7-82a7-95adb5ca38bd 태그 연결은 루틴의 완료 여부를 바꾸지 않는다") {
            listOf(false, true).forEach { isFinished ->
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                upsertTag(accountId, tag)
                val routine = insertRoutine(accountId = accountId, isFinished = isFinished)

                link(accountId = accountId, routineId = routine.id, tagId = tag.id)
                find(accountId = accountId, routineId = routine.id) shouldBe routine

                link(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
                find(accountId = accountId, routineId = routine.id) shouldBe routine
            }
        }

        test("TC-f16b6610-5540-40f1-82d4-9ba2780658ea 삭제 상태인 루틴에도 태그를 연결하고 해제하며 루틴은 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tag)
            val routine = insertRoutine(accountId = accountId, isDeleted = true)

            link(accountId = accountId, routineId = routine.id, tagId = tag.id)
            displayedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
            find(accountId = accountId, routineId = routine.id)?.isDeleted shouldBe true

            link(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            displayedTagIdList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
            find(accountId = accountId, routineId = routine.id)?.isDeleted shouldBe true
        }

        test("TC-2db49559-457b-4e6a-9ca8-9fbcdb444d31 태그 연결의 저장은 그 연결만 업로드 대기로 만들고 루틴은 대기로 만들지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tag)
            val routine = insertRoutine(accountId = accountId)
            val routineSyncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            val linkSyncDataSource = AccountEntityTagSyncLocalDataSourceImpl(database = database)

            link(accountId = accountId, routineId = routine.id, tagId = tag.id)
            routineSyncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            linkSyncDataSource.readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE).map { value -> value.tagId to value.isDeleted } shouldBe listOf(tag.id to false)

            link(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            routineSyncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            linkSyncDataSource.readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE).map { value -> value.tagId to value.isDeleted } shouldBe listOf(tag.id to true)
        }

        test("TC-821e6588-4ba1-40d7-a025-46e0cd125ad6 연결한 동안에는 그 태그의 루틴 목록에 나타나고 해제하면 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tag)
            val routine = insertRoutine(accountId = accountId)

            link(accountId = accountId, routineId = routine.id, tagId = tag.id)
            tagTabIdList(accountId = accountId, tagId = tag.id) shouldBe listOf(routine.id)

            link(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            tagTabIdList(accountId = accountId, tagId = tag.id).shouldBeEmpty()
        }

        test("TC-8c9755cc-bc89-4a5c-b166-834fae8d978c 필터가 없으면 연결 전과 같은 노출과 순서이고 태그 A를 고르면 루틴 X가 표시된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            upsertTag(accountId, tagA)
            val routineList = listOf("a-title", "b-title", "c-title").map { title -> insertRoutine(accountId = accountId, title = title) }
            val routineX = routineList[1]

            // 보기 범위가 처음 상태인 `오늘 할 루틴`일 때의 RoutineHome 목록이다. 루틴은 모두 주기가 매일이고 진행 중이다.
            suspend fun todayIdList(): List<Uuid> = database.todayRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }
            val before = todayIdList()
            before shouldBe routineList.map { routine -> routine.id }

            link(accountId = accountId, routineId = routineX.id, tagId = tagA.id)

            todayIdList() shouldBe before
            homeIdList(accountId) shouldBe before
            database.routineFilterTagDao().upsert(entity = RoutineFilterTagLocalEntity(accountId = accountId, tagId = tagA.id))
            todayIdList() shouldBe listOf(routineX.id)
            homeIdList(accountId) shouldBe listOf(routineX.id)
        }
    }) {
    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
    }
}
