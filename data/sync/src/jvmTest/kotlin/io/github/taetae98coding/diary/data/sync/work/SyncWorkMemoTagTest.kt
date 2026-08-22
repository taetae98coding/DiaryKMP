package io.github.taetae98coding.diary.data.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.mapper.memo.toLocal
import io.github.taetae98coding.diary.core.mapper.memotag.toLocal
import io.github.taetae98coding.diary.core.mapper.memotag.toRemote
import io.github.taetae98coding.diary.core.mapper.tag.toLocal
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class SyncWorkMemoTagTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-019 TC-MEMO-TAG-DATA-005 모든 태그와 메모 요청이 성공한 뒤 메모 태그 전용 요청을 시작한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 101),
                )
            val requestOrder = mutableListOf<String>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { requestOrder += "tag" }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { requestOrder += "memo" }
            coEvery { context.memoTagRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoTag" }

            context.subject.doWork(accountId = context.accountId)

            requestOrder shouldContainExactly listOf("tag", "memo", "memoTag", "memoTag")
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("메모 태그 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val memoTagList = memoTags(size = itemCount)
                val context = context(memoTagList = memoTagList)
                val requests = mutableListOf<List<MemoTagRemoteEntity>>()
                coEvery { context.memoTagRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<MemoTagRemoteEntity>>()
                }

                context.subject.doWork(accountId = context.accountId)

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly memoTagList.map { memoTag -> memoTag.toRemote() }
            }
        }

        test("메모 태그 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val memoTagList = memoTags(size = 101)
            val context = context(memoTagList = memoTagList)

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountMemoTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoTagList = memoTagList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoTagList = memoTagList.drop(100),
                )
            }
        }

        test("메모 태그 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(memoTagList = memoTags(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoTagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) { context.accountMemoTagSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.pull(any()) }
        }

        test("메모 태그 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = memoTagPulls(usnList = listOf(4L, 6L))
            val secondPullList = memoTagPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MEMO_TAG) } returns 2L
            coEvery { context.memoTagRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.memoTagRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.memoTagRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountMemoTagSyncTransaction.save(
                    accountId = context.accountId,
                    memoTagList = firstPullList.map { pull -> pull.memoTag.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoTagSyncTransaction.save(
                    accountId = context.accountId,
                    memoTagList = secondPullList.map { pull -> pull.memoTag.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.memoTagRemoteDataSource.pull(any()) }
        }

        test("메모 태그 내려받기가 지연되어도 태그와 메모 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.memoTagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "memoTagPullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
                    emptyList()
                }
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoPullEnd"
                    emptyList()
                }

                context.subject.doWork(accountId = context.accountId)

                callOrder shouldContainExactly listOf("tagPullEnd", "memoPullEnd", "memoTagPullEnd")
            }
        }

        test("메모 태그 내려받기가 실패해도 태그와 메모는 빈 응답까지 내려받고 동기화는 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagPullList = tagPulls(usnList = listOf(3L))
            val memoPullList = memoPulls(usnList = listOf(4L))
            coEvery { context.memoTagRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns emptyList()

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 3L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 4L,
                )
            }
            coVerify(exactly = 0) { context.accountMemoTagSyncTransaction.save(any(), any(), any()) }
        }

        test("요청에 전달된 계정의 메모 태그만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.memoTagSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns memoTags(size = 1)

            context.subject.doWork(accountId = accountId)

            coVerify(exactly = 1) {
                context.memoTagSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.memoTagSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
        }
    })
