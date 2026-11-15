package io.github.taetae98coding.diary.core.database.impl.routine.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineCycleAndUnitText
import io.github.taetae98coding.diary.core.database.impl.routinetag.routineTagTable
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class RoutineAddAccountRoutineTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountRoutineTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountRoutineTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

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

        test("TC-721ea497-efd4-4db1-b751-a7204dc77179 TC-e882b4d6-8f51-4e5a-b8ae-3911258664c7 루틴, 계정 연결과 고른 두 태그와의 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList = List(3) { Arb.localTag(isFinished = false, isDeleted = false).next() }
            insertTag(accountId, *tagList.toTypedArray())
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = tagList.take(2)))

            find(accountId = accountId, routineId = routine.id) shouldBe routine
            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldContainExactlyInAnyOrder tagList.take(2).map { tag -> tag.id }
        }

        test("TC-2bb300e0-0d65-4b6a-8660-4ea88218e211 저장에 실패하면 루틴, 계정 연결과 태그 연결 중 어느 것도 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = Arb.localRoutine(isDeleted = false).next()
            val failingDatabase = spyk(database)
            every { failingDatabase.accountRoutineDao() } throws RoutineAddTestException(Arb.string().next())

            shouldThrowExactly<RoutineAddTestException> {
                AccountRoutineTransactionImpl(database = failingDatabase).upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = listOf(tag)))
            }

            database.routineDao().findUpdatedAt(listOf(routine.id)).shouldBeEmpty()
            find(accountId = accountId, routineId = routine.id) shouldBe null
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            database.routineTagTable().findByEntityIdList(listOf(routine.id)).shouldBeEmpty()
        }

        test("TC-31824575-1c10-43c6-934f-00414514511e 저장한 루틴을 조회하면 저장한 정보가 그대로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val full = Arb.localRoutine(isDeleted = false).next().let { routine -> routine.copy(detail = routine.detail.copy(goalAmount = Arb.routineAmount().next())) }
            val blank = Arb.localRoutine(isDeleted = false).next().let { routine -> routine.copy(detail = routine.detail.copy(goalAmount = null)) }

            transaction.upsert(accountId = accountId, routine = full, routineTagList = emptyList())
            transaction.upsert(accountId = accountId, routine = blank, routineTagList = emptyList())

            find(accountId = accountId, routineId = full.id) shouldBe full
            val foundBlank = find(accountId = accountId, routineId = blank.id)
            foundBlank shouldBe blank
            foundBlank?.detail?.goalAmount shouldBe null
        }

        test("TC-f2be58a1-b7fe-4a40-bad1-6ba153b6ea52 고른 주기를 정해진 값으로 기록한다") {
            listOf(
                RoutineCycleLocalEntity.DAILY to "FREQ=DAILY",
                RoutineCycleLocalEntity.WEEKDAYS to "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR",
                RoutineCycleLocalEntity.WEEKENDS to "FREQ=DAILY;BYDAY=SA,SU",
                RoutineCycleLocalEntity.WEEKLY to "FREQ=WEEKLY;WKST=SU",
                RoutineCycleLocalEntity.MONTHLY to "FREQ=MONTHLY",
            ).forEach { (cycle, expectedText) ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(isDeleted = false).next().let { routine -> routine.copy(detail = routine.detail.copy(title = "물 마시기", cycle = cycle)) }

                transaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

                database.findRoutineCycleAndUnitText(routineId = routine.id).first shouldBe expectedText
            }
        }

        test("TC-6f442d59-63b6-4f58-b781-7060329ecdce 기록한 주기 값을 다시 같은 주기로 읽는다") {
            RoutineCycleLocalEntity.entries.forEach { cycle ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(isDeleted = false).next().let { routine -> routine.copy(detail = routine.detail.copy(cycle = cycle)) }

                transaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

                database.findRoutineCycleAndUnitText(routineId = routine.id).first shouldBe cycle.persistentValue
                find(accountId = accountId, routineId = routine.id)?.detail?.cycle shouldBe cycle
            }
        }

        test("TC-74cbf590-0f9e-473b-8bcb-9be297195b61 단위는 넘겨받은 글자 그대로 기록하고 빈 단위는 빈 글자로 기록한다") {
            listOf("ml", "분 걷기", "ML", "kilometers", "").forEach { unit ->
                val accountId = Arb.kotlinUuid().next()
                val routine = Arb.localRoutine(isDeleted = false).next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }

                transaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

                database.findRoutineCycleAndUnitText(routineId = routine.id).second shouldBe unit
                find(accountId = accountId, routineId = routine.id)?.detail?.unit shouldBe unit
            }
        }

        test("TC-e0d1f1aa-a05a-47ba-98f3-b09ea206f9f5 태그를 고르지 않으면 루틴만 저장되고 태그 연결은 하나도 만들어지지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            insertTag(accountId, Arb.localTag(isFinished = false, isDeleted = false).next())
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

            find(accountId = accountId, routineId = routine.id) shouldBe routine
            linkedTagIdList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
            database.routineTagTable().findByEntityIdList(listOf(routine.id)).shouldBeEmpty()
        }

        test("TC-2438f374-f8f7-4174-9a1c-065b002ce8bc 설명, 단위와 목표량이 없는 루틴도 그대로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine =
                Arb.localRoutine(isDeleted = false).next().let { routine ->
                    routine.copy(detail = routine.detail.copy(title = "운동", description = "", cycle = RoutineCycleLocalEntity.DAILY, unit = "", goalAmount = null, amountPerRecord = 1))
                }

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())

            val found = find(accountId = accountId, routineId = routine.id)
            found shouldBe routine
            found?.detail?.description shouldBe ""
            found?.detail?.unit shouldBe ""
            found?.detail?.goalAmount shouldBe null
            linkedTagIdList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
        }

        test("TC-36dfb227-5088-4d09-b747-05cfb52f54d5 저장한 루틴과 태그 연결이 각각 계정의 업로드 대기로 기록된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = listOf(tag)))

            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId) shouldBe listOf(routine)
            AccountEntityTagSyncLocalDataSourceImpl(database = database)
                .readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE)
                .map { link -> link.entityId to link.tagId } shouldBe listOf(routine.id to tag.id)
        }

        test("TC-a0a6c988-24fc-4cec-8603-5177c76149b0 고른 뒤 완료된 태그와의 연결도 저장되어 그 태그가 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            AccountTagTransactionImpl(database = database).updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = listOf(tag)))

            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
        }

        test("TC-a0a6c988-24fc-4cec-8603-5177c76149b0 고른 뒤 삭제된 태그와의 연결도 저장되어 태그의 삭제를 되돌리면 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val tagTransaction = AccountTagTransactionImpl(database = database)
            insertTag(accountId, tag)
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = listOf(tag)))

            linkedTagIdList(accountId = accountId, routineId = routine.id).shouldBeEmpty()
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
        }

        test("TC-fdbc35ac-e6b6-47b8-a61a-4819b8d07299 이미 완료된 태그와의 연결을 저장하면 그 태그가 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = true, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = Arb.localRoutine(isDeleted = false).next()

            transaction.upsert(accountId = accountId, routine = routine, routineTagList = linkList(routine = routine, tagList = listOf(tag)))

            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
        }

        test("TC-ca07dfdd-008f-41f1-8580-59b752caba54 같은 정보의 루틴도 다른 식별 정보로 저장하면 두 루틴이 모두 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val first = Arb.localRoutine(isDeleted = false).next()
            val second = first.copy(id = Arb.kotlinUuid().next())

            transaction.upsert(accountId = accountId, routine = first, routineTagList = emptyList())
            transaction.upsert(accountId = accountId, routine = second, routineTagList = emptyList())

            find(accountId = accountId, routineId = first.id) shouldBe first
            find(accountId = accountId, routineId = second.id) shouldBe second
        }
    }) {
    private companion object {
        fun linkList(
            routine: RoutineLocalEntity,
            tagList: List<TagLocalEntity>,
        ): List<EntityTagLocalEntity> =
            tagList.map { tag ->
                EntityTagLocalEntity(entityId = routine.id, tagId = tag.id, isDeleted = false, updatedAt = routine.updatedAt, createdAt = routine.createdAt)
            }
    }
}

private class RoutineAddTestException(
    message: String,
) : RuntimeException(message)
