package io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.cycleAmount
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.findAccountRoutineList
import io.github.taetae98coding.diary.core.database.impl.routine.findAccountRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.markRoutineUploaded
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

private class RoutineRecordTestException : RuntimeException()

class AccountRoutineRecordTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountRoutineRecordTransactionImpl

        beforeTest {
            database = routineTestDatabase()
            transaction = AccountRoutineRecordTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        // 업로드를 마친 루틴을 저장한다.
        suspend fun insertUploadedRoutine(
            accountId: Uuid,
            isDeleted: Boolean = false,
            isFinished: Boolean = false,
        ): RoutineLocalEntity {
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), isFinished = isFinished, isDeleted = isDeleted).next()
            database.insertRoutine(accountId, routine)
            database.markRoutineUploaded(accountId = accountId)

            return routine
        }

        suspend fun findRoutine(
            accountId: Uuid,
            routineId: Uuid,
        ): RoutineLocalEntity? = AccountRoutineLocalDataSourceImpl(database = database).find(accountId = accountId, routineId = routineId).first()

        test("TC-aaa79d00-61de-4f2c-969a-40589d5decd4 TC-4c532680-5a51-4c03-a37d-bee4981c14c8 TC-8887ca96-806e-4958-b37c-6c7601913865 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 기록 넣기는 새 기록과 계정 연결을 업로드 대기로 함께 저장하고 루틴은 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploadedRoutine(accountId)
            val record = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 3, 10), amount = 500).next()

            transaction.upsert(accountId = accountId, record = record) shouldBe 1

            database.findRoutineRecordList() shouldBe listOf(record)
            database.findAccountRoutineRecordList() shouldBe listOf(AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = record.id, isDirty = true))
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine
            database.findAccountRoutineList() shouldBe listOf(AccountRoutineLocalEntity(accountId = accountId, routineId = routine.id, isDirty = false))
        }

        test("TC-4c532680-5a51-4c03-a37d-bee4981c14c8 TC-34479437-32c2-4d78-84ce-d20748c95e38 기록 넣기의 저장이 실패하면 새 기록과 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploadedRoutine(accountId)
            val failingDatabase = spyk(database)
            every { failingDatabase.accountRoutineRecordDao() } throws RoutineRecordTestException()

            shouldThrowExactly<RoutineRecordTestException> {
                AccountRoutineRecordTransactionImpl(database = failingDatabase).upsert(
                    accountId = accountId,
                    record = Arb.localRoutineRecord(routineId = routine.id).next(),
                )
            }

            database.findRoutineRecordList().shouldBeEmpty()
            database.findAccountRoutineRecordList().shouldBeEmpty()
        }

        test("TC-11b44b3b-5add-4404-a4c6-ce258d78e169 TC-851c367c-ac29-47f2-ba80-30a3faae119d 한 루틴의 같은 날짜 기록은 하나로 합쳐 두지 않고 각각 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val date = LocalDate(2026, 11, 13)
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 11, 1)).next()
            database.insertRoutine(accountId, routine)
            val first = Arb.localRoutineRecord(routineId = routine.id, date = date, amount = 200).next()
            val second = Arb.localRoutineRecord(routineId = routine.id, date = date, amount = 200).next()

            transaction.upsert(accountId = accountId, record = first) shouldBe 1
            transaction.upsert(accountId = accountId, record = second) shouldBe 1

            database.findRoutineRecordList() shouldContainExactlyInAnyOrder listOf(first, second)
            database.calendarRoutineRecordList(accountId = accountId, start = date, endInclusive = date).map { calendarRecord -> calendarRecord.amount } shouldBe listOf(400L)

            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.updateDeleted(accountId = accountId, recordId = second.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            database.findRoutineRecordList() shouldContainExactlyInAnyOrder listOf(first, second.copy(isDeleted = true, updatedAt = updatedAt))
            database.calendarRoutineRecordList(accountId = accountId, start = date, endInclusive = date).map { calendarRecord -> calendarRecord.amount } shouldBe listOf(200L)
        }

        test("TC-8f20a50e-651a-42a3-9276-61ff22c596a3 날짜가 루틴의 시작 날짜보다 앞인 기록도 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 11, 10)).next()
            database.insertRoutine(accountId, routine)
            val before = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 11, 9), amount = 1).next()
            val after = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 11, 21), amount = 1).next()

            transaction.upsert(accountId = accountId, record = before) shouldBe 1
            transaction.upsert(accountId = accountId, record = after) shouldBe 1

            database.findRoutineRecordList() shouldContainExactlyInAnyOrder listOf(before, after)
        }

        test("TC-ebd9deee-73ac-4b54-9806-3bb3e9f9c38a 완료된 루틴에도 기록을 넣고 빼며 루틴은 완료된 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploadedRoutine(accountId, isFinished = true)
            val record = Arb.localRoutineRecord(routineId = routine.id, date = LocalDate(2026, 7, 15), amount = 250).next()

            transaction.upsert(accountId = accountId, record = record) shouldBe 1
            database.findRoutineRecordList() shouldBe listOf(record)
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine

            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1
            database.findRoutineRecordList() shouldBe listOf(record.copy(isDeleted = true, updatedAt = updatedAt))
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine
        }

        test("TC-f16b6610-5540-40f1-82d4-9ba2780658ea TC-8b41ff69-57ea-4181-88d1-370b18d77ab6 삭제 상태인 루틴에도 기록을 넣고 빼며 루틴은 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploadedRoutine(accountId, isDeleted = true)
            val record = Arb.localRoutineRecord(routineId = routine.id).next()

            transaction.upsert(accountId = accountId, record = record) shouldBe 1
            database.findRoutineRecordList() shouldBe listOf(record)
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine

            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1
            database.findRoutineRecordList() shouldBe listOf(record.copy(isDeleted = true, updatedAt = updatedAt))
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine
        }

        test("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 다른 계정과만 연결되었거나 기기에 없는 루틴에는 기록을 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val otherRoutine = insertUploadedRoutine(otherAccountId)

            transaction.upsert(accountId = accountId, record = Arb.localRoutineRecord(routineId = otherRoutine.id).next()) shouldBe 0
            transaction.upsert(accountId = accountId, record = Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next()).next()) shouldBe 0

            database.findRoutineRecordList().shouldBeEmpty()
            database.findAccountRoutineRecordList().shouldBeEmpty()
            findRoutine(accountId = otherAccountId, routineId = otherRoutine.id) shouldBe otherRoutine
        }

        test("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 다른 계정과만 연결되었거나 기기에 없는 기록은 빼지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val otherRoutine = insertUploadedRoutine(otherAccountId)
            val otherRecord = database.insertRoutineRecord(accountId = otherAccountId, routineId = otherRoutine.id, date = LocalDate(2026, 3, 10), amount = 1)
            database.markRoutineUploaded(accountId = otherAccountId)

            transaction.updateDeleted(accountId = accountId, recordId = otherRecord.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, recordId = Arb.kotlinUuid().next(), isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            database.findRoutineRecordList() shouldBe listOf(otherRecord)
            database.findAccountRoutineRecordList() shouldBe listOf(AccountRoutineRecordLocalEntity(accountId = otherAccountId, routineRecordId = otherRecord.id, isDirty = false))
        }

        test(
            "TC-66ca9007-c763-4a22-8cf6-66a583355e88 TC-da48468e-ae2a-45bd-b406-de0dac89755d TC-1aee5238-a247-4058-ba98-22b71d774ca9 TC-8887ca96-806e-4958-b37c-6c7601913865 TC-d513ab4c-84b3-4ea4-8309-6d39e08fc80e 기록 빼기는 그 기록의 삭제 여부와 수정 시각만 바꿔 업로드 대기로 남긴다",
        ) {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 7, 15)
            val routine = insertUploadedRoutine(accountId)
            val record = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 250)
            val otherRecord = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 100)
            database.markRoutineUploaded(accountId = accountId)
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            database.findRoutineRecordList() shouldContainExactlyInAnyOrder listOf(record.copy(isDeleted = true, updatedAt = updatedAt), otherRecord)
            database.findAccountRoutineRecordList() shouldContainExactlyInAnyOrder
                listOf(
                    AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = record.id, isDirty = true),
                    AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = otherRecord.id, isDirty = false),
                )
            AccountRoutineRecordSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { pending -> pending.id } shouldBe listOf(record.id)
            findRoutine(accountId = accountId, routineId = routine.id) shouldBe routine
            database.findAccountRoutineList() shouldBe listOf(AccountRoutineLocalEntity(accountId = accountId, routineId = routine.id, isDirty = false))
            database.cycleAmount(accountId = accountId, routineId = routine.id, today = today) shouldBe 100L
            database.calendarRoutineRecordList(accountId = accountId, start = today, endInclusive = today).map { calendarRecord -> calendarRecord.amount } shouldBe listOf(100L)
        }

        test("TC-34479437-32c2-4d78-84ce-d20748c95e38 기록 빼기의 저장이 실패하면 기록이 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = insertUploadedRoutine(accountId)
            val record = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 10), amount = 1)
            database.markRoutineUploaded(accountId = accountId)
            val failingDao = spyk(database.accountRoutineRecordDao())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountRoutineRecordDao() } returns failingDao
            coEvery { failingDao.markPending(accountId = any(), recordId = any()) } throws RoutineRecordTestException()

            shouldThrowExactly<RoutineRecordTestException> {
                AccountRoutineRecordTransactionImpl(database = failingDatabase).updateDeleted(
                    accountId = accountId,
                    recordId = record.id,
                    isDeleted = true,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }

            database.findRoutineRecordList() shouldBe listOf(record)
            database.findAccountRoutineRecordList() shouldBe listOf(AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = record.id, isDirty = false))
        }

        test("TC-514569dc-a29f-4ea6-9e90-d327eb7edfcc 루틴 기록을 넣거나 빼도 루틴과 태그의 연결은 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList = List(2) { Arb.localTag(isFinished = false, isDeleted = false).next() }
            database.insertRoutineTestTag(accountId, *tagList.toTypedArray())
            val routine = insertUploadedRoutine(accountId)
            tagList.forEach { tag -> database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id) }
            val entityTagDataSource = AccountEntityTagLocalDataSourceImpl(database = database)

            suspend fun linkedTagIdList(): List<Uuid> = entityTagDataSource.getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id).first().map { tag -> tag.id }
            val before = linkedTagIdList()
            before shouldContainExactlyInAnyOrder tagList.map { tag -> tag.id }
            val record = Arb.localRoutineRecord(routineId = routine.id).next()

            transaction.upsert(accountId = accountId, record = record) shouldBe 1
            linkedTagIdList() shouldBe before

            transaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            linkedTagIdList() shouldBe before
        }
    })
