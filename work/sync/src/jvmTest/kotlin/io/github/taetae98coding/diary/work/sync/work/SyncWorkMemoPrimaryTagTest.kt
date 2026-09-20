package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.coVerify
import kotlin.uuid.Uuid

class SyncWorkMemoPrimaryTagTest :
    FunSpec({
        listOf(
            "대표 태그가 없는" to null,
            "대표 태그가 있는" to fixtureMonkey.giveMeOne<Uuid>(),
        ).forEach { (label, primaryTagId) ->
            test("TC-MEMO-PRIMARY-TAG-DATA-003 $label 메모를 업로드하면 그 대표 태그가 요청에 담긴다") {
                val memoList = listOf(memo().copy(primaryTagId = primaryTagId))
                val context = context(memoList = memoList)
                val requests = mutableListOf<List<MemoRemoteEntity>>()
                coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<MemoRemoteEntity>>()
                }

                context.subject.doWork()

                requests.flatten().map { memo -> memo.primaryTagId } shouldContainExactly listOf(primaryTagId)
            }
        }

        test("TC-MEMO-PRIMARY-TAG-DATA-005 대표 태그는 메모 종류의 요청과 내려받기 위치로만 오간다") {
            val pendingMemo = memo().copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
            val pulledMemo = memo().copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
            val context = context(memoList = listOf(pendingMemo))
            coEvery { context.memoRemoteDataSource.pull(0L) } returns
                listOf(MemoPullRemoteEntity(memo = pulledMemo.toRemote(), usn = 1L))
            coEvery { context.memoRemoteDataSource.pull(1L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.memoRemoteDataSource.push(listOf(pendingMemo.toRemote()))
                context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MEMO)
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = listOf(pulledMemo.toRemote().toLocal()),
                    cursor = 1L,
                )
            }
            coVerify(exactly = 0) {
                context.tagRemoteDataSource.push(any())
                context.memoTagRemoteDataSource.push(any())
            }
        }
    })
