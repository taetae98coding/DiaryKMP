package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.data.testing.dday.localDDay
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify

class SyncWorkDDayPrimaryTagTest :
    FunSpec({
        listOf(
            "대표 태그가 없는" to false,
            "대표 태그가 있는" to true,
        ).forEach { (label, hasPrimaryTag) ->
            test("TC-ef9ab8da-d0ab-46fb-8a64-b2155478acfb $label 디데이를 업로드하면 그 대표 태그가 요청에 담긴다") {
                val primaryTagId = if (hasPrimaryTag) Arb.kotlinUuid().next() else null
                val stored = Arb.localDDay(isDeleted = Arb.boolean().next()).next().copy(primaryTagId = primaryTagId)
                val context = context(ddayList = listOf(stored))
                val requestList = mutableListOf<List<DDayRemoteEntity>>()
                coEvery { context.ddayRemoteDataSource.push(any()) } answers { requestList += firstArg<List<DDayRemoteEntity>>() }

                context.subject.doWork()

                requestList.flatten().map { dday -> dday.primaryTagId } shouldContainExactly listOf(primaryTagId)
            }
        }

        listOf(
            "대표 태그가 없으면" to false,
            "대표 태그가 있으면" to true,
        ).forEach { (label, hasPrimaryTag) ->
            test("TC-2566329d-2444-42ed-847f-02e8ebadca54 내려받은 디데이에 $label 그 대표 태그로 기기에 저장한다") {
                val primaryTagId = if (hasPrimaryTag) Arb.kotlinUuid().next() else null
                val response =
                    Arb
                        .localDDay(isDeleted = Arb.boolean().next())
                        .next()
                        .toRemote()
                        .copy(primaryTagId = primaryTagId)
                val context = context()
                coEvery { context.ddayRemoteDataSource.pull(usn = 0L) } returns listOf(DDayPullRemoteEntity(dday = response, usn = 1L))
                coEvery { context.ddayRemoteDataSource.pull(usn = 1L) } returns emptyList()
                val savedList = mutableListOf<List<DDayLocalEntity>>()
                coEvery { context.accountDDaySyncTransaction.upsert(any(), any(), any()) } answers { savedList += secondArg<List<DDayLocalEntity>>() }

                context.subject.doWork()

                savedList.flatten().map { dday -> dday.primaryTagId } shouldContainExactly listOf(primaryTagId)
            }
        }

        test("TC-0dc4f03f-c4f7-4f28-b166-2c9e32f44a02 대표 태그는 디데이 종류의 요청과 내려받기 위치로만 오간다") {
            val pendingDDay = Arb.localDDay(isDeleted = false).next().copy(primaryTagId = Arb.kotlinUuid().next())
            val pulledDDay = Arb.localDDay(isDeleted = false).next().copy(primaryTagId = Arb.kotlinUuid().next())
            val context = context(ddayList = listOf(pendingDDay))
            coEvery { context.ddayRemoteDataSource.pull(usn = 0L) } returns listOf(DDayPullRemoteEntity(dday = pulledDDay.toRemote(), usn = 1L))
            coEvery { context.ddayRemoteDataSource.pull(usn = 1L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.ddayRemoteDataSource.push(listOf(pendingDDay.toRemote()))
                context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.DDAY)
                context.accountDDaySyncTransaction.upsert(
                    accountId = context.accountId,
                    ddayList = listOf(pulledDDay.toRemote().toLocal()),
                    cursor = 1L,
                )
            }
            coVerify(exactly = 0) {
                context.tagRemoteDataSource.push(any())
                context.entityTagRemoteDataSource.push(any(), any())
            }
        }
    })
