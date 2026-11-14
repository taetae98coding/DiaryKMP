package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify

// 추가·수정·삭제와 기록 넣기·빼기로 업로드 대기가 된 루틴과 루틴 기록을 서버에 올리는 동기화의 결과를 확인한다.
class RoutineDetailSyncWorkTest :
    FunSpec({
        listOf(false, true).forEach { isDeleted ->
            test("TC-36dfb227-5088-4d09-b747-05cfb52f54d5 TC-8887ca96-806e-4958-b37c-6c7601913865 TC-091631e1-6514-4f40-b33a-87d5eeb743a4 업로드 대기인 ${if (isDeleted) "삭제된 " else ""}루틴을 올리고 성공하면 대기를 해제한다") {
                val routine = Arb.localRoutine(isDeleted = isDeleted).next()
                val context = context(routineList = listOf(routine))
                val pushedList = mutableListOf<List<RoutineRemoteEntity>>()
                coEvery { context.routineRemoteDataSource.push(any()) } answers { pushedList += firstArg<List<RoutineRemoteEntity>>() }

                context.subject.doWork()

                pushedList shouldBe listOf(listOf(routine.toRemote()))
                coVerify(exactly = 1) { context.accountRoutineSyncTransaction.clearPending(accountId = context.accountId, routineList = listOf(routine)) }
            }

            test("TC-00d23677-06fd-4541-bde3-7288b7928e7e TC-0e6f404f-0e42-4df9-a75a-f94cc5c71169 서버 반영이 실패하면 ${if (isDeleted) "삭제된 " else ""}루틴의 업로드 대기를 해제하지 않는다") {
                val context = context(routineList = listOf(Arb.localRoutine(isDeleted = isDeleted).next()))
                coEvery { context.routineRemoteDataSource.push(any()) } throws TestException(Arb.string().next())

                shouldThrowExactly<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) { context.accountRoutineSyncTransaction.clearPending(any(), any()) }
            }
        }

        listOf(false, true).forEach { isDeleted ->
            test("TC-8887ca96-806e-4958-b37c-6c7601913865 TC-d513ab4c-84b3-4ea4-8309-6d39e08fc80e 업로드 대기인 ${if (isDeleted) "뺀 " else ""}루틴 기록을 루틴 없이 올리고 성공하면 대기를 해제한다") {
                val record = Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = isDeleted).next()
                val context = context(routineRecordList = listOf(record))
                val pushedList = mutableListOf<List<RoutineRecordRemoteEntity>>()
                coEvery { context.routineRecordRemoteDataSource.push(any()) } answers { pushedList += firstArg<List<RoutineRecordRemoteEntity>>() }

                context.subject.doWork()

                pushedList shouldBe listOf(listOf(record.toRemote()))
                coVerify(exactly = 0) { context.routineRemoteDataSource.push(any()) }
                coVerify(exactly = 1) { context.accountRoutineRecordSyncTransaction.clearPending(accountId = context.accountId, recordList = listOf(record)) }
            }

            test("TC-0e6f404f-0e42-4df9-a75a-f94cc5c71169 서버 반영이 실패하면 ${if (isDeleted) "뺀 " else ""}루틴 기록의 업로드 대기를 해제하지 않는다") {
                val context = context(routineRecordList = listOf(Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = isDeleted).next()))
                coEvery { context.routineRecordRemoteDataSource.push(any()) } throws TestException(Arb.string().next())

                shouldThrowExactly<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) { context.accountRoutineRecordSyncTransaction.clearPending(any(), any()) }
            }
        }
    })
