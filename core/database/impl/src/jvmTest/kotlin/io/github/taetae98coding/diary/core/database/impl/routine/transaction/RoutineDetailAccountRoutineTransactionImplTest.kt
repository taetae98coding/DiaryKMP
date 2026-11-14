package io.github.taetae98coding.diary.core.database.impl.routine.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountTagRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.findAccountRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineCycleAndUnitText
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.finishedRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.markRoutineUploaded
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.todayRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.search.datasource.SearchRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.routine.routineDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class RoutineDetailAccountRoutineTransactionImplTest :
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

        suspend fun insertUploaded(
            accountId: Uuid,
            isDeleted: Boolean,
            tagList: List<TagLocalEntity> = emptyList(),
            isFinished: Boolean = false,
            title: String? = null,
        ): RoutineLocalEntity {
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = title, isFinished = isFinished, isDeleted = isDeleted).next()
            transaction.upsert(
                accountId = accountId,
                routine = routine,
                routineTagList =
                    tagList.map { tag ->
                        EntityTagLocalEntity(entityId = routine.id, tagId = tag.id, isDeleted = false, updatedAt = routine.updatedAt, createdAt = routine.createdAt)
                    },
            )
            val syncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            AccountRoutineSyncTransactionImpl(database = database).clearPending(accountId = accountId, routineList = syncDataSource.readPendingList(accountId = accountId))
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            return routine
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

        suspend fun homeIdList(accountId: Uuid): List<Uuid> = database.routineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

        suspend fun insertRecordList(
            accountId: Uuid,
            routine: RoutineLocalEntity,
        ): List<RoutineRecordLocalEntity> {
            val recordList =
                listOf(false, true).map { isDeleted ->
                    database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next(), isDeleted = isDeleted)
                }
            database.markRoutineUploaded(accountId = accountId)

            return recordList
        }

        test("TC-0ba8553c-17da-4bd4-acf4-b81e0b1cf676 삭제 상태인 루틴도 상세 대상으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploaded(accountId = accountId, isDeleted = true)

            find(accountId = accountId, routineId = routine.id) shouldBe routine
        }

        test("TC-59993333-fe5a-4248-84d9-7a7ca26b660e 다른 계정과만 연결된 루틴은 없고 현재 계정의 루틴은 저장한 정보로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val mine = insertUploaded(accountId = accountId, isDeleted = false)
            val others = insertUploaded(accountId = otherAccountId, isDeleted = false)

            find(accountId = accountId, routineId = others.id) shouldBe null
            find(accountId = accountId, routineId = mine.id) shouldBe mine
        }

        test("TC-0ad47719-cf1c-40ff-923f-f53f466bd97f TC-c20c094f-8b07-4373-aeff-5845f58e9f0b TC-26a743be-a098-43ca-a381-f17a6e065650 TC-8887ca96-806e-4958-b37c-6c7601913865 수정은 정보와 수정 시각만 바꾸고 업로드 대기로 기록한다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
            val recordList = insertRecordList(accountId = accountId, routine = routine)
            val detail = Arb.routineDetailLocalEntity().next()
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(detail = detail, updatedAt = updatedAt)
            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            AccountRoutineRecordSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { value -> value.id } shouldBe listOf(routine.id)
        }

        test("TC-2dc8c2c6-a0de-4168-bbec-206a8a59d62d 수정은 주기를 정해진 값으로, 목표량과 한 번에 기록할 양을 수로 기록한다") {
            listOf(
                Triple(RoutineCycleLocalEntity.WEEKLY, 3, 1) to "FREQ=WEEKLY;WKST=SU",
                Triple(RoutineCycleLocalEntity.MONTHLY, 12_000, 500) to "FREQ=MONTHLY",
                Triple(RoutineCycleLocalEntity.DAILY, null, 9_999_999) to "FREQ=DAILY",
                Triple(RoutineCycleLocalEntity.WEEKDAYS, 20, 5) to "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR",
                Triple(RoutineCycleLocalEntity.WEEKENDS, null, 1) to "FREQ=DAILY;BYDAY=SA,SU",
            ).forEach { (given, cycleText) ->
                val (cycle, goalAmount, amountPerRecord) = given
                val accountId = Arb.kotlinUuid().next()
                val routine = insertUploaded(accountId = accountId, isDeleted = false)
                val detail = routine.detail.copy(cycle = cycle, goalAmount = goalAmount, amountPerRecord = amountPerRecord)

                transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = detail, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

                database.findRoutineCycleAndUnitText(routineId = routine.id).first shouldBe cycleText
                find(accountId = accountId, routineId = routine.id)?.detail shouldBe detail
            }
        }

        test("TC-97a23bfd-edd0-425c-aa19-eb13804e0ec8 수정은 단위를 넘겨받은 글자 그대로 기록하고 빈 단위는 빈 글자로 기록한다") {
            listOf("L", "분 걷기", "ML", "").forEach { unit ->
                val accountId = Arb.kotlinUuid().next()
                val routine = insertUploaded(accountId = accountId, isDeleted = false).let { routine -> routine.copy(detail = routine.detail.copy(unit = "ml")) }
                transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = routine.detail, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

                transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = routine.detail.copy(unit = unit), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

                database.findRoutineCycleAndUnitText(routineId = routine.id).second shouldBe unit
                find(accountId = accountId, routineId = routine.id)?.detail?.unit shouldBe unit
            }
        }

        test("TC-f16b6610-5540-40f1-82d4-9ba2780658ea 삭제 상태인 루틴을 수정해도 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploaded(accountId = accountId, isDeleted = true)
            val detail = Arb.routineDetailLocalEntity().next()

            transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = detail, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            find(accountId = accountId, routineId = routine.id)?.isDeleted shouldBe true
            find(accountId = accountId, routineId = routine.id)?.detail shouldBe detail
        }

        listOf(false, true).forEach { isFinished ->
            test("TC-84a50df2-a720-4cb8-a237-2e8cf05ac891 완료 여부가 ${isFinished}인 루틴을 수정해도 완료 여부는 그대로다") {
                val accountId = Arb.kotlinUuid().next()
                val routine = insertUploaded(accountId = accountId, isDeleted = false, isFinished = isFinished)
                val detail = routine.detail.copy(title = Arb.string().next(), startDate = Arb.kotlinxLocalDate().next(), goalAmount = Arb.routineAmount().next())
                val updatedAt = Arb.secondPrecisionInstant().next()

                transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = detail, updatedAt = updatedAt) shouldBe 1

                find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(detail = detail, updatedAt = updatedAt)
                find(accountId = accountId, routineId = routine.id)?.isFinished shouldBe isFinished
            }
        }

        test("TC-a7d11800-4d0e-430e-a7bf-51770909236f TC-1aee5238-a247-4058-ba98-22b71d774ca9 TC-8887ca96-806e-4958-b37c-6c7601913865 삭제는 삭제 여부와 수정 시각만 바꾸고 목록에서 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
            val recordList = insertRecordList(accountId = accountId, routine = routine)
            homeIdList(accountId) shouldBe listOf(routine.id)
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isDeleted = true, updatedAt = updatedAt)
            linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
            homeIdList(accountId).shouldBeEmpty()
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
            database.findAccountRoutineRecordList().map { link -> link.routineRecordId } shouldContainExactlyInAnyOrder recordList.map { record -> record.id }
            AccountRoutineRecordSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { value -> value.id } shouldBe listOf(routine.id)
        }

        test("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 다른 계정과만 연결된 루틴은 수정하거나 삭제하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routine = insertUploaded(accountId = otherAccountId, isDeleted = false)

            transaction.updateDetail(accountId = accountId, routineId = routine.id, detail = Arb.routineDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            find(accountId = otherAccountId, routineId = routine.id) shouldBe routine
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = otherAccountId).shouldBeEmpty()
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 기기에 없는 루틴은 수정하거나 삭제하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val stored = insertUploaded(accountId = accountId, isDeleted = false)
            val missingId = Arb.kotlinUuid().next()

            transaction.updateDetail(accountId = accountId, routineId = missingId, detail = Arb.routineDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, routineId = missingId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            find(accountId = accountId, routineId = stored.id) shouldBe stored
            find(accountId = accountId, routineId = missingId) shouldBe null
            AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
        }

        listOf(false, true).forEach { isFinished ->
            val action = if (isFinished) "다시 시작" else "완료"

            test("TC-bbb6db55-01ee-4859-928c-5b2005951fbe TC-4c0b2f7f-b581-4920-b478-908e0fa377c3 ${action}은 완료 여부와 수정 시각만 바꾸고 기록과 태그 연결을 남긴다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(accountId, tag)
                val routine = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag), isFinished = isFinished)
                val recordList = insertRecordList(accountId = accountId, routine = routine)
                val updatedAt = Arb.secondPrecisionInstant().next()

                transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = !isFinished, updatedAt = updatedAt) shouldBe 1

                find(accountId = accountId, routineId = routine.id) shouldBe routine.copy(isFinished = !isFinished, updatedAt = updatedAt)
                linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
                database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
                database.findAccountRoutineRecordList().map { link -> link.routineRecordId } shouldContainExactlyInAnyOrder recordList.map { record -> record.id }
                AccountRoutineRecordSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
                AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { value -> value.id } shouldBe listOf(routine.id)
            }

            test("TC-a67df1e5-b918-49da-85e7-b6a1308e808d 다른 계정과만 연결되거나 기기에 없는 루틴은 ${action}하지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val otherAccountId = Arb.kotlinUuid().next()
                val stored = insertUploaded(accountId = accountId, isDeleted = false, isFinished = isFinished)
                val otherAccountRoutine = insertUploaded(accountId = otherAccountId, isDeleted = false, isFinished = isFinished)

                listOf(otherAccountRoutine.id, Arb.kotlinUuid().next()).forEach { routineId ->
                    transaction.updateFinished(accountId = accountId, routineId = routineId, isFinished = !isFinished, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
                }

                find(accountId = accountId, routineId = stored.id) shouldBe stored
                find(accountId = otherAccountId, routineId = otherAccountRoutine.id) shouldBe otherAccountRoutine
                AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
                AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = otherAccountId).shouldBeEmpty()
            }

            test("TC-6c70fa47-52f4-4b00-b897-c505b3706104 $action 저장에 실패하면 저장된 루틴의 완료 여부가 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val routine = insertUploaded(accountId = accountId, isDeleted = false, isFinished = isFinished)
                val failingDao = spyk(database.accountRoutineDao())
                coEvery { failingDao.markPending(accountId = any(), routineId = any()) } throws RoutineDetailTestException(Arb.string().next())
                val failingDatabase = spyk(database)
                every { failingDatabase.accountRoutineDao() } returns failingDao

                shouldThrowExactly<RoutineDetailTestException> {
                    AccountRoutineTransactionImpl(database = failingDatabase).updateFinished(
                        accountId = accountId,
                        routineId = routine.id,
                        isFinished = !isFinished,
                        updatedAt = Arb.secondPrecisionInstant().next(),
                    )
                }

                find(accountId = accountId, routineId = routine.id) shouldBe routine
                AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            }
        }

        test("TC-fc08b823-2f3d-4726-b043-f629f6b7f3cc TC-b0702c99-3b47-4b6d-99e4-8639278db8cc 완료하면 완료된 루틴 목록으로 옮겨 가고 다시 시작하면 되돌아온다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val routine = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag), title = "물 마시기")
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = 1)
            val currentCycle = routineCurrentCycleOf(today = TODAY)
            val tagDataSource = AccountTagRoutineLocalDataSourceImpl(database = database)

            // 보기 범위가 처음 상태인 `오늘 할 루틴`일 때의 RoutineHome 목록이다.
            suspend fun homeIdList(): List<Uuid> = database.todayRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

            suspend fun finishedIdList(): List<Uuid> = database.finishedRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

            suspend fun tagIdList(): List<Uuid> = tagDataSource.page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF, currentCycle = currentCycle).loadAll().map { summary -> summary.routine.id }

            suspend fun tagFinishedIdList(): List<Uuid> = tagDataSource.pageFinished(accountId = accountId, tagId = tag.id, currentCycle = currentCycle).loadAll().map { summary -> summary.routine.id }

            suspend fun searchedFinishedList(): List<Boolean> = SearchRoutineLocalDataSourceImpl(database = database).page(accountId = accountId, query = "물", currentCycle = currentCycle).loadAll().map { summary -> summary.routine.isFinished }

            suspend fun calendarIdList(): List<Uuid> = database.calendarRoutineRecordList(accountId = accountId).map { record -> record.routineId }

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            homeIdList().shouldBeEmpty()
            tagIdList().shouldBeEmpty()
            finishedIdList() shouldBe listOf(routine.id)
            tagFinishedIdList() shouldBe listOf(routine.id)
            searchedFinishedList() shouldBe listOf(true)
            calendarIdList() shouldBe listOf(routine.id)

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            homeIdList() shouldBe listOf(routine.id)
            tagIdList() shouldBe listOf(routine.id)
            finishedIdList().shouldBeEmpty()
            tagFinishedIdList().shouldBeEmpty()
            searchedFinishedList() shouldBe listOf(false)
            calendarIdList() shouldBe listOf(routine.id)
        }

        listOf(true, false).forEach { isDelete ->
            test("TC-34479437-32c2-4d78-84ce-d20748c95e38 ${if (isDelete) "삭제" else "내용 수정"} 저장에 실패하면 저장된 루틴과 태그 연결이 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(accountId, tag)
                val routine = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
                val failingDao = spyk(database.accountRoutineDao())
                coEvery { failingDao.markPending(accountId = any(), routineId = any()) } throws RoutineDetailTestException(Arb.string().next())
                val failingDatabase = spyk(database)
                every { failingDatabase.accountRoutineDao() } returns failingDao
                val failingTransaction = AccountRoutineTransactionImpl(database = failingDatabase)

                shouldThrowExactly<RoutineDetailTestException> {
                    if (isDelete) {
                        failingTransaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
                    } else {
                        failingTransaction.updateDetail(accountId = accountId, routineId = routine.id, detail = Arb.routineDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next())
                    }
                }

                find(accountId = accountId, routineId = routine.id) shouldBe routine
                linkedTagIdList(accountId = accountId, routineId = routine.id) shouldBe listOf(tag.id)
                AccountRoutineSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            }
        }
    }) {
    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
    }
}

private class RoutineDetailTestException(
    message: String,
) : RuntimeException(message)
