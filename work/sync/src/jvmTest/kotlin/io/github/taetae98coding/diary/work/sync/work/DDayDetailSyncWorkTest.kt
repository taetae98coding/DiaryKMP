package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify

// 추가·수정·삭제로 업로드 대기가 된 디데이를 서버에 올리는 동기화의 결과를 확인한다.
class DDayDetailSyncWorkTest :
    FunSpec({
        listOf(false, true).forEach { isDeleted ->
            test("TC-59804ef8-703f-41a2-b0fd-7747b4210ab8 TC-439ff23c-cd9a-42eb-9348-dcbfb47a7fc4 업로드 대기인 ${if (isDeleted) "삭제된 " else ""}디데이를 올리고 성공하면 대기를 해제한다") {
                val dday = Arb.localDDay(isDeleted = isDeleted).next()
                val context = context(ddayList = listOf(dday))
                val pushedList = mutableListOf<List<DDayRemoteEntity>>()
                coEvery { context.ddayRemoteDataSource.push(any()) } answers { pushedList += firstArg<List<DDayRemoteEntity>>() }

                context.subject.doWork()

                pushedList shouldBe listOf(listOf(dday.toRemote()))
                coVerify(exactly = 1) { context.accountDDaySyncTransaction.clearPending(accountId = context.accountId, ddayList = listOf(dday)) }
            }

            test("TC-77b71541-e31d-4ec9-af09-8b60ec5771d0 TC-7174af3d-700c-4233-9d55-dc036e0677dc 서버 반영이 실패하면 ${if (isDeleted) "삭제된 " else ""}디데이의 업로드 대기를 해제하지 않는다") {
                val context = context(ddayList = listOf(Arb.localDDay(isDeleted = isDeleted).next()))
                coEvery { context.ddayRemoteDataSource.push(any()) } throws TestException(Arb.string().next())

                shouldThrowExactly<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) { context.accountDDaySyncTransaction.clearPending(any(), any()) }
            }
        }
    })
